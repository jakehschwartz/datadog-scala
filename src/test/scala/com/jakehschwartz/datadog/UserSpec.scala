package com.jakehschwartz.datadog

import akka.http.scaladsl.model.{HttpEntity, HttpMethods}
import org.json4s._
import org.json4s.native.JsonMethods._
import org.specs2.mutable.Specification

import scala.concurrent.Await
import scala.concurrent.duration._

class UserSpec extends Specification {

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

    "handle invite users" in {
      val res = Await.result(client.inviteUsers(Seq("friend@example.com")), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest.get.uri.toString must be_==("https://app.datadoghq.com/api/v1/invite_users?api_key=apiKey&application_key=appKey")
      adapter.getRequest.get.method must be_==(HttpMethods.POST)

      val body = parse(adapter.getRequest.get.entity.asInstanceOf[HttpEntity.Strict].getData().utf8String)
      (body \ "emails") (0).extract[String] must beEqualTo("friend@example.com")
    }
  }
}