package com.lvxingpai.idgen

import com.google.inject.Inject
import com.mongodb.BasicDBObject
import com.mongodb.client.model.{ FindOneAndUpdateOptions, ReturnDocument, UpdateOptions }
import com.twitter.util.{ Future, FuturePool }

/**
 * Created by zephyre on 11/18/15.
 */
class IdGenHandler @Inject() (factory: MongoClientFactory) extends IdGen.FutureIface {
  implicit lazy val defaultFuturePool = FuturePool.unboundedPool

  implicit lazy val defaultExecutionContext = scala.concurrent.ExecutionContext
    .fromExecutorService(defaultFuturePool.executor)

  val coll = factory.getDatabase.getCollection("Counter")

  override def generate(generator: String): Future[Long] = {
    defaultFuturePool {
      val query = new BasicDBObject("_id", generator)
      val update = new BasicDBObject("$inc", new BasicDBObject("counter", 1L))
      val opt = new FindOneAndUpdateOptions() upsert true returnDocument ReturnDocument.AFTER
      val ret = coll.findOneAndUpdate(query, update, opt)
      ret.get("counter") match {
        case x: java.lang.Long => Long.unbox(x)
        case x =>
          assert(assertion = false, s"Invalid counter: $x")
          -1L
      }
    }
  }

  override def getCounter(generator: String): Future[Long] = {
    defaultFuturePool {
      val query = new BasicDBObject("_id", generator)
      val counter = Option(coll.find(query).first()) map (d => {
        d.get("counter") match {
          case x: java.lang.Long => Long.unbox(x)
          case x =>
            assert(assertion = false, s"Invalid counter: $x")
            -1L
        }
      }) getOrElse 0L
      counter
    }
  }

  override def resetCounter(generator: String, level: Long): Future[Unit] = {
    defaultFuturePool {
      val query = new BasicDBObject("_id", generator)
      val update = new BasicDBObject("$set", new BasicDBObject("counter", level))
      val opt = new UpdateOptions() upsert true
      coll.updateOne(query, update, opt)
    }
  }

  override def ping(): Future[String] = {
    defaultFuturePool("pong")
  }
}
