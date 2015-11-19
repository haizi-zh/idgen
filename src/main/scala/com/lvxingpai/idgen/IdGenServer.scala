package com.lvxingpai.idgen

import java.net.InetSocketAddress

import com.google.inject.name.Names
import com.google.inject.{ AbstractModule, Guice, Key }
import com.lvxingpai.etcd.EtcdStoreModule
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import com.typesafe.config.{ Config, ConfigException, ConfigFactory }
import org.apache.thrift.protocol.TBinaryProtocol.Factory

import scala.util.Try

/**
 * Created by zephyre on 11/18/15.
 */
object IdGenServer extends App {
  main(args)

  override def main(args: Array[String]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val injector = Guice.createInjector(new EtcdStoreModule(ConfigFactory.defaultApplication()))
    val handler = injector.getInstance(classOf[IdGenHandler])
    val conf = injector.getInstance(Key.get(classOf[Config], Names.named("etcdConf")))

    for {
      port <- Try(conf getInt "idgen.port") recover {
        case _: ConfigException.Missing => 9000
      }
      maxConcur <- Try(conf getInt "idgen.maxConcur") recover {
        case _: ConfigException.Missing => 1000
      }
    } yield {
      val service = new IdGen$FinagleService(handler, new Factory())
      ServerBuilder()
        .bindTo(new InetSocketAddress(port))
        .codec(ThriftServerFramedCodec())
        .name("idgen")
        .maxConcurrentRequests(maxConcur)
        .build(service)
    }
  }
}

