package com.jakehschwartz.datadog

import akka.http.scaladsl.model.{HttpEntity, HttpMethods}
import org.json4s._
import org.specs2.mutable.Specification

import scala.concurrent.Await
import scala.concurrent.duration._

class AlertSpec extends Specification {

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

    "handle get all alerts" in {
      val res = Await.result(client.getAllAlerts, Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest.get.uri.toString must be_==("https://app.datadoghq.com/api/v1/alert?api_key=apiKey&application_key=appKey")
      adapter.getRequest.get.method must be_==(HttpMethods.GET)
    }

    "handle add alert" in {
      val res = Await.result(client.addAlert("POOP"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest.get.uri.toString must be_==("https://app.datadoghq.com/api/v1/alert?api_key=apiKey&application_key=appKey")
      adapter.getRequest.get.entity.asInstanceOf[HttpEntity.Strict].getData().utf8String must be_==("POOP")

      adapter.getRequest.get.method must be_==(HttpMethods.POST)
    }

    "handle get alert" in {
      val res = Await.result(client.getAlert(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest.get.uri.toString must be_==("https://app.datadoghq.com/api/v1/alert/12345?api_key=apiKey&application_key=appKey")
      adapter.getRequest.get.method must be_==(HttpMethods.GET)
    }

    "handle delete alert" in {
      val res = Await.result(client.deleteAlert(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest.get.uri.toString must be_==("https://app.datadoghq.com/api/v1/alert/12345?api_key=apiKey&application_key=appKey")

      adapter.getRequest.get.method must be_==(HttpMethods.DELETE)
    }

    "handle update screenboard" in {
      val res = Await.result(client.updateAlert(12345, "POOP"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest.get.uri.toString must be_==("https://app.datadoghq.com/api/v1/alert/12345?api_key=apiKey&application_key=appKey")
      adapter.getRequest.get.entity.asInstanceOf[HttpEntity.Strict].getData().utf8String must be_==("POOP")

      adapter.getRequest.get.method must be_==(HttpMethods.PUT)
    }

    "handle mute all alerts" in {
      val res = Await.result(client.muteAllAlerts, Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest.get.uri.toString must be_==("https://app.datadoghq.com/api/v1/mute_alerts?api_key=apiKey&application_key=appKey")

      adapter.getRequest.get.method must be_==(HttpMethods.POST)
    }

    "handle unmute all alerts" in {
      val res = Await.result(client.unmuteAllAlerts, Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest.get.uri.toString must be_==("https://app.datadoghq.com/api/v1/unmute_alerts?api_key=apiKey&application_key=appKey")

      adapter.getRequest.get.method must be_==(HttpMethods.POST)
    }
  }
}