package com.hortonworks.orendainx.trucking.schemaregistry

import com.hortonworks.registries.schemaregistry.SchemaMetadata.Builder
import com.hortonworks.registries.schemaregistry.{SchemaCompatibility, SchemaMetadata, SchemaVersion, SerDesInfo}
import com.hortonworks.registries.schemaregistry.avro.AvroSchemaProvider
import com.hortonworks.registries.schemaregistry.client.SchemaRegistryClient
import com.hortonworks.registries.schemaregistry.serdes.avro.{AvroSnapshotDeserializer, AvroSnapshotSerializer}
import com.typesafe.scalalogging.Logger

import scala.collection.mutable

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object Registry {
  val log = Logger(this.getClass)

  def main(args: Array[String]): Unit = {

    val clientConfig = mutable.HashMap[String, AnyRef]()
    clientConfig += (SchemaRegistryClient.Configuration.SCHEMA_REGISTRY_URL.name() -> "http://sandbox.hortonworks.com:8091/api/v1")







    val serdesJarName = "/schema/schema-registry-serdes-0.1.0-SNAPSHOT.jar"
    val jarInputStream = getClass.getResourceAsStream(serdesJarName)

    import collection.JavaConversions._
    val registryClient = new SchemaRegistryClient(clientConfig)

    val fileId = registryClient.uploadFile(jarInputStream)

    val serializerInfo = new SerDesInfo.Builder()
      .name("someserializername")
      .description("someserializerdesc")
      .fileId(fileId)
      .className("org.apache.registries.schemaregistry.serdes.avro.AvroSnapshotSerializer")
      .buildSerializerInfo()
    val serializerId = registryClient.addSerializer(serializerInfo)

    val deserializerInfo = new SerDesInfo.Builder()
      .name("somedesname")
      .description("someDesDesc")
      .fileId(fileId)
      .className("org.apache.registries.schemaregistry.serdes.avro.AvroSnapshotDeserializer")
      .buildDeserializerInfo()
    val deserializerId = registryClient.addDeserializer(deserializerInfo)


    // instead of uploading jar, TRY default ser/des
    // this returns any generic type ... so cast for now
    //val defaultSerializer = registryClient.getDefaultSerializer(AvroSchemaProvider.TYPE).asInstanceOf[AvroSnapshotSerializer]
    //val defaultDeserializer = registryClient.getDefaultDeserializer(AvroSchemaProvider.TYPE).asInstanceOf[AvroSnapshotDeserializer]


    // Register schema metadata
    val schemaGroupName = "schemaGroupName"
    val schemaName = "schemaName"
    val schemaType = AvroSchemaProvider.TYPE

    val description = "schemaDescription"
    val compatibility = SchemaCompatibility.BACKWARD
    val schemaFileName = "/schema/trucking-event.avsc"

    val schemaMetadata = new SchemaMetadata.Builder(schemaName)
      .`type`(schemaType)
      .schemaGroup(schemaGroupName)
      .description(description)
      .compatibility(compatibility)
      .build()

    val status = registryClient.registerSchemaMetadata(schemaMetadata)
    log.info(s"Schema registration: $status")




    // add schema version
    val schemaStream = this.getClass.getResourceAsStream(schemaFileName)

    val scanner = new java.util.Scanner(schemaStream).useDelimiter("\\A")
    val schemaText = if (scanner.hasNext) scanner.next() else ""

    log.info(s"Schema is: $schemaText")
    val schemaVersion = new SchemaVersion(schemaText, "SchemaVersionText")
    val version = registryClient.addSchemaVersion(schemaName, schemaVersion)
    // TODO: can also addschemaversion(schemametadata) instead of schemaName .... huh.

    log.info(s"Schema version id: $version")

    registryClient.mapSchemaWithSerDes(schemaName, serializerId)
    registryClient.mapSchemaWithSerDes(schemaName, deserializerId)
  }
}

class Registry {
  //
}
