package com.lvxingpai.idgen

import com.google.inject._
import com.lvxingpai.configuration.Configuration
import com.lvxingpai.etcd.EtcdStoreModule

/**
 * Created by zephyre on 12/19/15.
 */
object Injector {
  lazy val instance = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val module = new AbstractModule {
      override def configure(): Unit = bind(classOf[IdGenHandler]) toProvider new Provider[IdGenHandler] {
        lazy val get = new IdGenHandler
      }
    }
    Guice.createInjector(new EtcdStoreModule(Configuration.load()), module)
  }
}
