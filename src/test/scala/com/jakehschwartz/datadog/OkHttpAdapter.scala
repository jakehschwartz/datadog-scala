package com.jakehschwartz.datadog

import akka.http.scaladsl.model.HttpRequest

import scala.concurrent.Future

class OkHttpAdapter extends HttpAdapter {

  var lastRequest: Option[HttpRequest] = None

  override def doHttp(request: HttpRequest) = {
    lastRequest = Some(request)
    Future {
      Response(200, "Ok")
    }
  }

  def getRequest = lastRequest
}

