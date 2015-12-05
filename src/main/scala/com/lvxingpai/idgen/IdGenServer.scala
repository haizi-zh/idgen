package com.lvxingpai.idgen

import java.net.InetSocketAddress

import com.google.inject.name.Names
import com.google.inject.{ Guice, Key }
import com.lvxingpai.configuration.Configuration
import com.lvxingpai.etcd.EtcdStoreModule
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import com.typesafe.config.{ ConfigFactory, Config }
import org.apache.thrift.protocol.TBinaryProtocol.Factory

/**
 * Created by zephyre on 11/18/15.
 */
object IdGenServer extends App {
  main(args)

  override def main(args: Array[String]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val injector = Guice.createInjector(new EtcdStoreModule(Configuration.load()))
    val handler = injector.getInstance(classOf[IdGenHandler])
    val conf = injector.getInstance(Key.get(classOf[Configuration], Names.named("etcdConf")))

    val port = conf getInt "idgen.port" getOrElse 9000
    val maxConcur = conf getInt "idgen.maxConcur" getOrElse 1000
    val name = conf getString "idgen.serverName" getOrElse "idgen"
    val service = new IdGen$FinagleService(handler, new Factory())
    ServerBuilder()
      .bindTo(new InetSocketAddress(port))
      .codec(ThriftServerFramedCodec())
      .name(name)
      .maxConcurrentRequests(maxConcur)
      .build(service)
  }
}

