package com.hortonworks.orendainx.trucking.schemaregistry

import java.util.Scanner

import com.hortonworks.registries.schemaregistry.{SchemaCompatibility, SchemaMetadata, SchemaVersion, SerDesInfo}
import com.hortonworks.registries.schemaregistry.avro.AvroSchemaProvider
import com.hortonworks.registries.schemaregistry.client.SchemaRegistryClient
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger

/**
  * Example of how to leverage the Schema Registry with Scala.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object SchemaRegistryDemo {

  // Create logger
  val log = Logger(this.getClass)

  def main(args: Array[String]): Unit = {

    // Load default configuration file (application.conf in the resources folder)
    val config = ConfigFactory.load()


    /*
     * Create a config object with the configuration properties to instantiate a SchemaRegistryClient
     */
    //val clientConfig = mutable.HashMap[String, AnyRef]()
    val clientConfig = new java.util.HashMap[String, AnyRef]()
    val schemaRegistryUrl = config.getString("schema-registry.url")
    clientConfig.put(SchemaRegistryClient.Configuration.SCHEMA_REGISTRY_URL.name(), schemaRegistryUrl)

    val schemaRegistryClient = new SchemaRegistryClient(clientConfig)




    /*
     * Retrieve configuration properties for the general schema information,
     * using it to create and then registering the schema metadata with the registry client.
     */
    val schemaName = config.getString("schema.name")
    val schemaGroupName = config.getString("schema.group-name")
    val schemaDescription = config.getString("schema.description")
    val schemaTypeCompatibility = SchemaCompatibility.BACKWARD
    val schemaType = AvroSchemaProvider.TYPE

    val schemaMetadata = new SchemaMetadata.Builder(schemaName)
      .`type`(schemaType)
      .schemaGroup(schemaGroupName)
      .description(schemaDescription)
      .compatibility(schemaTypeCompatibility)
      .build()

    val metadataRegistrationResult = schemaRegistryClient.registerSchemaMetadata(schemaMetadata)

    log.info(s"Schema registration result: $metadataRegistrationResult")




    /*
     * Retrieve configuration properties for the Avro version of the schema,
     * then upload the jar file with the compiled class files of the de/serializer.
     * Follow up with creating both de/serializers, and finally add them with the registry client.
     */
    val avroSchemaName = config.getString("schema.avro.name")
    val avroSchemaDescription = config.getString("schema.avro.description")
    val avroSerializerClassName = config.getString("schema.avro.serializer-class-name")
    val avroDeserializerClassName = config.getString("schema.avro.deserializer-class-name")

    val avroJarPath = config.getString("schema.avro.jarpath")
    val avroJar = getClass.getResourceAsStream(avroJarPath)
    val avroSerDesFileId = schemaRegistryClient.uploadFile(avroJar)

    val avroSerializerInfo = new SerDesInfo.Builder()
      .name(avroSchemaName)
      .description(avroSchemaDescription)
      .fileId(avroSerDesFileId)
      .className(avroSerializerClassName)
      .buildSerializerInfo()

    val avroDeserializerInfo = new SerDesInfo.Builder()
      .name(avroSchemaName)
      .description(avroSchemaDescription)
      .fileId(avroSerDesFileId)
      .className(avroDeserializerClassName)
      .buildDeserializerInfo()

    val avroSerializerId = schemaRegistryClient.addSerializer(avroSerializerInfo)
    val avroDeserializerId = schemaRegistryClient.addDeserializer(avroDeserializerInfo)

    log.info(s"Avro serializer id: $avroSerializerId")
    log.info(s"Avro deserializer id: $avroDeserializerId")


    /*
     * Read the file that has the the Avro schema content, creating a SchemaVersion out of it
     * before adding that version with the registry client.
     */
    val avroSchemaFilepath = config.getString("schema.avro.filepath")
    val avroSchemaInputStream = this.getClass.getResourceAsStream(avroSchemaFilepath)
    val scanner = new Scanner(avroSchemaInputStream).useDelimiter("\\A")
    val avroSchemaContent = if (scanner.hasNext) scanner.next() else ""

    val schemaVersion = new SchemaVersion(avroSchemaContent, "Initial schema")
    val schemaVersionId = schemaRegistryClient.addSchemaVersion(schemaName, schemaVersion)
    //val schemaVersionId = schemaRegistryClient.addSchemaVersion(schemaMetadata, schemaVersion)

    log.info(s"Schema content: $avroSchemaContent")
    log.info(s"Schema version id: $schemaVersionId")

    schemaRegistryClient.mapSchemaWithSerDes(schemaName, avroSerializerId)
    schemaRegistryClient.mapSchemaWithSerDes(schemaName, avroDeserializerId)
  }




  // Cast, since this returns an untyped object.
  //val defaultSerializer = schemaRegistryClient.getDefaultSerializer(AvroSchemaProvider.TYPE).asInstanceOf[AvroSnapshotSerializer]
  //val defaultDeserializer = schemaRegistryClient.getDefaultDeserializer(AvroSchemaProvider.TYPE).asInstanceOf[AvroSnapshotDeserializer]
}
