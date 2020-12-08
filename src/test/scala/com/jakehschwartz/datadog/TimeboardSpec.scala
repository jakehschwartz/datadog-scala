package com.jakehschwartz.datadog

import akka.http.scaladsl.model.{HttpEntity, HttpMethods}
import org.json4s._
import org.specs2.mutable.Specification

import scala.concurrent.Await
import scala.concurrent.duration._

class TimeboardSpec extends Specification {

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

    "handle get all timeboards" in {
      val res = Await.result(client.getAllTimeboards, Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest.get.uri.toString must be_==("https://app.datadoghq.com/api/v1/dash?api_key=apiKey&application_key=appKey")
      adapter.getRequest.get.method must be_==(HttpMethods.GET)
    }

    "handle add timeboard" in {
      val res = Await.result(client.addTimeboard("POOP"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest.get.uri.toString must be_==("https://app.datadoghq.com/api/v1/dash?api_key=apiKey&application_key=appKey")
      adapter.getRequest.get.entity.asInstanceOf[HttpEntity.Strict].getData().utf8String must be_==("POOP")

      adapter.getRequest.get.method must be_==(HttpMethods.POST)
    }

    "handle get timeboard" in {
      val res = Await.result(client.getTimeboard(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest.get.uri.toString must be_==("https://app.datadoghq.com/api/v1/dash/12345?api_key=apiKey&application_key=appKey")
      adapter.getRequest.get.method must be_==(HttpMethods.GET)
    }

    "handle delete timeboard" in {
      val res = Await.result(client.deleteTimeboard(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest.get.uri.toString must be_==("https://app.datadoghq.com/api/v1/dash/12345?api_key=apiKey&application_key=appKey")

      adapter.getRequest.get.method must be_==(HttpMethods.DELETE)
    }

    "handle update timeboard" in {
      val res = Await.result(client.updateTimeboard(12345, "POOP"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest.get.uri.toString must be_==("https://app.datadoghq.com/api/v1/dash/12345?api_key=apiKey&application_key=appKey")
      adapter.getRequest.get.entity.asInstanceOf[HttpEntity.Strict].getData().utf8String must be_==("POOP")

      adapter.getRequest.get.method must be_==(HttpMethods.PUT)
    }
  }
}