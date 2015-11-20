package com.lvxingpai.idgen

import com.google.inject.Inject
import com.google.inject.name.Named
import com.lvxingpai.configuration.Configuration
import com.mongodb.client.MongoDatabase
import com.mongodb.{ MongoClient, MongoCredential, ServerAddress }

import scala.collection.JavaConversions._

/**
 * Created by zephyre on 11/18/15.
 */
class MongoClientFactory @Inject() (@Named("etcdService") val services: Configuration, @Named("etcdConf") val conf: Configuration) {
  def getDatabase: MongoDatabase = {
    (for {
      db <- conf getString "idgen.mongo.database"
    } yield {
      val instance = conf getString "idgen.mongo.instance" getOrElse "mongo"
      val serverAddresses = ((services getConfig s"services.$instance") getOrElse Configuration.empty)
        .underlying.root().entrySet().toSeq map (entry => {
          val tmp = entry.getValue atKey "root"
          val host = tmp getString "root.host"
          val port = tmp getInt "root.port"
          new ServerAddress(host, port)
        })

      val userOpt = conf getString "idgen.mongo.user"
      val passwordOpt = conf getString "idgen.mongo.password"
      val adminSource = conf getString "idgen.mongo.adminSource" getOrElse db
      val client = (for {
        user <- userOpt
        password <- passwordOpt
      } yield {
        val credential = MongoCredential.createCredential(user, adminSource, password.toCharArray)
        new MongoClient(serverAddresses, Seq(credential))
      }) getOrElse {
        new MongoClient(serverAddresses)
      }
      client.getDatabase(db)
    }).get
  }
}
