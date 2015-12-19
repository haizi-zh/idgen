package com.lvxingpai.idgen.http

import java.net.InetSocketAddress

import com.google.inject.Key
import com.google.inject.name.Names
import com.lvxingpai.configuration.Configuration
import com.lvxingpai.idgen.Injector
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http.{ Http, Request, Response, Status }
import com.twitter.finagle.{ Service, SimpleFilter }

/**
 * Created by zephyre on 12/19/15.
 */
object HttpServer {
  def start(): Unit = {

    /**
     * A simple Filter that catches exceptions and converts them to appropriate
     * HTTP responses.
     */
    class HandleExceptions extends SimpleFilter[Request, Response] {
      def apply(request: Request, service: Service[Request, Response]) = {
        // `handle` asynchronously handles exceptions.
        service(request) handle {
          case error: Throwable =>
            error.printStackTrace()
            val errorResponse = Response(Status.InternalServerError)
            errorResponse.contentString = error.getMessage
            errorResponse
        }
      }
    }

    val conf = Injector.instance getInstance Key.get(classOf[Configuration], Names named "etcd")

    val port = conf getInt "idgen.http.port" getOrElse 9020
    val maxConcur = conf getInt "idgen.http.maxConcur" getOrElse 1000
    val name = conf getString "idgen.serverName" getOrElse "idgen-http"
    val service = new HandleExceptions andThen new IdGenService()

    ServerBuilder()
      .bindTo(new InetSocketAddress(port))
      .codec(Http())
      .name(name)
      .maxConcurrentRequests(maxConcur)
      .build(service)
  }
}
