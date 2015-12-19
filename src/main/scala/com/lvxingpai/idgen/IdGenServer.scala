package com.lvxingpai.idgen

import com.lvxingpai.idgen.http.HttpServer
import com.lvxingpai.idgen.thrift.ThriftServer
import com.twitter.logging.Logger

/**
 * Created by zephyre on 11/18/15.
 */
object IdGenServer extends App {
  val logger = Logger("idgen")
  logger.info("Starting the HTTP server...")
  val httpServer = HttpServer.start()
  logger.info("HTTP server started.")

  logger.info("Starting the Thrift server...")
  val thriftServer = ThriftServer.start()
  logger.info("Thrift server started.")

  logger.info("Waiting for connections...")
}

