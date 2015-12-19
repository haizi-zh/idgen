package com.lvxingpai.idgen.thrift

import java.net.InetSocketAddress

import com.google.inject.Key
import com.google.inject.name.Names
import com.lvxingpai.configuration.Configuration
import com.lvxingpai.idgen.{ IdGen$FinagleService, IdGenHandler, Injector }
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import org.apache.thrift.protocol.TBinaryProtocol.Factory

/**
 * Created by zephyre on 12/19/15.
 */
object ThriftServer {
  def start(): Unit = {
    val injector = Injector.instance
    val handler = injector getInstance classOf[IdGenHandler]
    val conf = injector getInstance Key.get(classOf[Configuration], Names named "etcd")

    val port = conf getInt "idgen.thrift.port" getOrElse 9000
    val maxConcur = conf getInt "idgen.thrift.maxConcur" getOrElse 1000
    val name = conf getString "idgen.thrift.serverName" getOrElse "idgen-thrift"
    val service = new IdGen$FinagleService(handler, new Factory())
    ServerBuilder()
      .bindTo(new InetSocketAddress(port))
      .codec(ThriftServerFramedCodec())
      .name(name)
      .maxConcurrentRequests(maxConcur)
      .build(service)
  }
}
