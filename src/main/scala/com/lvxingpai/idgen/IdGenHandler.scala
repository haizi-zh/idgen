package com.lvxingpai.idgen

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.name.Names
import com.google.inject.{ Inject, Key }
import com.lvxingpai.configuration.Configuration
import com.lvxingpai.idgen.TwitterConverters.scalaToTwitterFuture
import com.twitter.util.Future

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Created by zephyre on 11/18/15.
 */
class IdGenHandler @Inject() extends IdGen.FutureIface {
  private val system = ActorSystem("idgen")

  {
    val factory = Injector.instance.getInstance(classOf[MongoClientFactory])
    system.actorOf(Props(classOf[DatastoreActor], factory), "datastore")
  }

  private val actorMap = scala.collection.mutable.Map[String, ActorRef]()

  override def ping(): Future[String] = Future("pong")

  override def getCounter(generator: String): Future[Long] = null

  override def generate(generator: String): Future[Long] = {
    val actor = if (actorMap contains generator) {
      actorMap(generator)
    } else {
      this.synchronized {
        val conf = Injector.instance.getInstance(Key.get(classOf[Configuration], Names.named("etcdConf")))
        val batchSize = conf getInt "idgen.batchSize" getOrElse 40
        val result = system.actorOf(Props(classOf[GeneratorActor], generator, batchSize), generator)
        actorMap.put(generator, result)
      }
      actorMap(generator)
    }

    implicit val timeout = Timeout(10 seconds)
    (actor ? "generate").mapTo[Long]
  }

  override def resetCounter(generator: String, level: Long): Future[Unit] = null
}
