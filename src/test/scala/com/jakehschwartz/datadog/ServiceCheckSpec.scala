package com.jakehschwartz.datadog

import akka.http.scaladsl.model.HttpMethods
import org.json4s._
import org.specs2.mutable.Specification

import scala.concurrent.Await
import scala.concurrent.duration._

class ServiceCheckSpec extends Specification {

  implicit val formats = DefaultFormats

  // Sequential because it's less work to share the client instance
  sequential

  "Client" should {

    val adapter = new OkHttpAdapter()
    val client = new Client(
      apiKey = "apiKey",
      appKey = "appKey",
      httpAdapter = adapter
    )

    "handle add service check" in {
      val res = Await.result(
        client.addServiceCheck(
          check = "app.is_ok", hostName = "app1", status = 0
        ), Duration(5, "second")
      )

      res.statusCode must beEqualTo(200)

      val uri = adapter.getRequest.get.uri.toString
      uri must contain("https://app.datadoghq.com/api/v1/check_run")

      val params = adapter.getRequest.get.uri.query().toMap
      params must havePairs(
        "api_key" -> "apiKey",
        "application_key" -> "appKey",
        "check" -> "app.is_ok",
        "host_name" -> "app1",
        "status" -> "0"
      )

      adapter.getRequest.get.method must be_==(HttpMethods.POST)
    }
  }
}
