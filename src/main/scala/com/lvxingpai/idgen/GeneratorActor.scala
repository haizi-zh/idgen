package com.lvxingpai.idgen

import akka.actor.Actor
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Created by zephyre on 12/5/15.
 */
class GeneratorActor(generatorName: String, batchSize: Int = 100) extends Actor {
  assert(batchSize >= 2)

  val log = Logging(context.system, this)

  var idPool = scala.collection.mutable.ListBuffer[Long]()

  val ds = {
    val system = context.system
    system.actorSelection(system / "datastore")
  }

  implicit val timeout = Timeout(10 seconds)

  private def handleAllocation(x: Long): Unit = {
    // 获得新分配的空间
    log.info(s"Id pool allocated: from ${x - batchSize} to $x")
    idPool ++= Range(0, batchSize) map (x - batchSize + _)
  }

  def receive = {
    case x: Long => handleAllocation(x)
    case "allocate" =>
      // 分配
      ds ! DatastoreActor.BatchGenerate(generatorName, batchSize)
    case "generate" =>
      // 是否需要新获取一些数据?
      val sdr = sender()
      if (idPool.isEmpty) {
        log.warning("Id pool is empty. Try to allocate.")
        val future = (ds ? DatastoreActor.BatchGenerate(generatorName, batchSize)).mapTo[Long]
        val result = Await.result(future, 10 seconds)
        handleAllocation(result)
      } else if (idPool.length < batchSize / 2) {
        self ! "allocate"
      }

      val result = idPool.head
      idPool -= result
      sdr ! result
  }
}
