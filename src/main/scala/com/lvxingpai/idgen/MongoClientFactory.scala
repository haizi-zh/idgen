package com.lvxingpai.idgen

import com.google.inject.Inject
import com.google.inject.name.Named
import com.mongodb.client.MongoDatabase
import com.mongodb.{ MongoClient, MongoCredential, ServerAddress }
import com.typesafe.config.{ Config, ConfigException }

import scala.collection.JavaConversions._
import scala.util.Try

/**
 * Created by zephyre on 11/18/15.
 */
class MongoClientFactory @Inject() (@Named("etcdService") val services: Config, @Named("etcdConf") val conf: Config) {
  def getDatabase: MongoDatabase = {
    val serverAddresses = (services getConfig "services.mongo").root().entrySet().toSeq map (entry => {
      val name = entry.getKey
      val tmp = entry.getValue.atKey("root")
      val host = tmp getString "root.host"
      val port = tmp getInt "root.port"
      new ServerAddress(host, port)
    })

    (for {
      db <- Try(conf getString "idgen.mongo.db")
      userOpt <- Try(conf getString "idgen.mongo.user") map Option.apply recover {
        case _: ConfigException.Missing => None
      }
      passwordOpt <- Try(conf getString "idgen.mongo.password") map Option.apply recover {
        case _: ConfigException.Missing => None
      }
    } yield {
      val client = (for {
        user <- userOpt
        password <- passwordOpt
      } yield {
        val credential = MongoCredential.createCredential(user, db, password.toCharArray)
        new MongoClient(serverAddresses, Seq(credential))
      }) getOrElse {
        new MongoClient(serverAddresses)
      }

      client.getDatabase(db)
    }).get
  }
}
