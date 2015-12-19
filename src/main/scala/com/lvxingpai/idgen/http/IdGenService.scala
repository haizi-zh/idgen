package com.lvxingpai.idgen.http

import java.util.Date

import com.fasterxml.jackson.databind.ObjectMapper
import com.lvxingpai.idgen.{ IdGenHandler, Injector }
import com.twitter.finagle.http.path._
import com.twitter.finagle.http.{ Method, Request, Response, Status }
import com.twitter.finagle.{ Service, http }
import com.twitter.logging.Logger
import com.twitter.util.Future

/**
 * Created by zephyre on 12/19/15.
 */
class IdGenService extends Service[http.Request, http.Response] {
  private val logger = Logger("app")

  private val mapper = new ObjectMapper

  /**
   * 为Response添加一些共有的属性
   * @param response
   * @param contentType
   * @return
   */
  def decorateResponse(response: Response, contentType: String = "application/json"): Response = {
    response.contentType = contentType
    response.charset = "utf-8"
    response.headerMap.add("Connection", "keep-alive")
    response.headerMap.add("Date", new Date())
    response
  }

  override def apply(request: Request): Future[Response] = {
    val method = request.method
    val path = Path(request.path)

    // 为无效的请求构造响应
    def responseForInvalidReq(message: Option[String] = None): Response = {
      val response = request.response
      response.setStatusCode(Status.UnprocessableEntity.code)
      response.setContentString(message getOrElse "")
      response
    }

    (method, path) match {
      case Method.Post -> Root / "generators" / generator / "ids" =>
        val handler = Injector.instance getInstance classOf[IdGenHandler]
        handler.generate(generator) map (newId => {
          logger.info(s"Id generated for $generator: $newId")
          val node = mapper.createObjectNode()
          node.put("generator", generator).put("id", newId)
          val response = request.response
          response.contentString = mapper.writeValueAsString(node)
          decorateResponse(response)
        })
      case Method.Get -> Root / "ping" =>
        val response = request.response
        response.contentString = "pong"
        Future(decorateResponse(response, "text/plain"))
      case _ =>
        Future(responseForInvalidReq())
    }
  }
}
