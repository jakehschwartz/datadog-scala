package com.jakehschwartz.datadog

import akka.http.scaladsl.model.{HttpEntity, HttpMethods}
import org.json4s._
import org.json4s.native.JsonMethods._
import org.specs2.mutable.Specification

import scala.concurrent.Await
import scala.concurrent.duration._

class TagSpec extends Specification {

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

    "handle get all tags" in {
      val res = Await.result(client.getAllTags, Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest.get.uri.toString must be_==("https://app.datadoghq.com/api/v1/tags/hosts?api_key=apiKey&application_key=appKey")
      adapter.getRequest.get.method must be_==(HttpMethods.GET)
    }

    "handle add tags for host" in {
      val res = Await.result(client.addTags("12345", Seq("foo:bar", "butt")), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest.get.uri.toString must be_==("https://app.datadoghq.com/api/v1/tags/hosts/12345?api_key=apiKey&application_key=appKey")
      adapter.getRequest.get.method must be_==(HttpMethods.POST)

      val body = parse(adapter.getRequest.get.entity.asInstanceOf[HttpEntity.Strict].getData().utf8String)
      (body \ "tags") (0).extract[String] must beEqualTo("foo:bar")
      (body \ "tags") (1).extract[String] must beEqualTo("butt")
    }

    "handle get tags for host" in {
      val res = Await.result(client.getTags("12345"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest.get.uri.toString must be_==("https://app.datadoghq.com/api/v1/tags/hosts/12345?api_key=apiKey&application_key=appKey")
      adapter.getRequest.get.method must be_==(HttpMethods.GET)
    }

    "handle delete tags" in {
      val res = Await.result(client.deleteTags("12345"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest.get.uri.toString must be_==("https://app.datadoghq.com/api/v1/tags/hosts/12345?api_key=apiKey&application_key=appKey")

      adapter.getRequest.get.method must be_==(HttpMethods.DELETE)
    }
  }
}