package com.lvxingpai.idgen

import akka.actor.Actor
import com.lvxingpai.idgen.DatastoreActor.{ BatchGenerate, GetGeneratorLevel }
import com.mongodb.BasicDBObject
import com.mongodb.client.model.{ FindOneAndUpdateOptions, ReturnDocument }

/**
 * Created by zephyre on 12/5/15.
 */
class DatastoreActor(factory: MongoClientFactory) extends Actor {
  lazy val coll = factory.getDatabase.getCollection("Counter")

  def receive = {
    case GetGeneratorLevel(generatorName) =>
      val query = new BasicDBObject("_id", generatorName)
      val counter = Option(coll.find(query).first()) map (d => {
        d.get("counter") match {
          case x: java.lang.Number => x.longValue()
          case x =>
            assert(assertion = false, s"Invalid counter: $x")
            -1L
        }
      }) getOrElse 0L
      sender() ! counter
    case BatchGenerate(generatorName, inc) =>
      val query = new BasicDBObject("_id", generatorName)
      val update = new BasicDBObject("$inc", new BasicDBObject("counter", inc.toLong))
      val opt = new FindOneAndUpdateOptions() upsert true returnDocument ReturnDocument.AFTER
      val ret = coll.findOneAndUpdate(query, update, opt)
      val counter = ret.get("counter") match {
        case x: java.lang.Number => x.longValue()
        case x =>
          assert(assertion = false, s"Invalid counter: $x")
          -1L
      }
      sender() ! counter
  }
}

object DatastoreActor {

  case class GetGeneratorLevel(generatorName: String)

  case class BatchGenerate(generatorName: String, inc: Long)

}
