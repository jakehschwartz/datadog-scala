package com.jakehschwartz.datadog

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model.HttpMethods
import akka.util.Timeout
import org.json4s._
import org.specs2.mutable.Specification

import scala.concurrent.Await
import scala.concurrent.duration._

class ScreenboardSpec extends Specification {

  implicit val formats = DefaultFormats

  // Sequential because it's less work to share the client instance
  sequential

  "Screenboard Client" should {

    val adapter = new OkHttpAdapter()
    val client = new Client(
      apiKey = "apiKey",
      appKey = "appKey",
      httpAdapter = adapter
    )

    implicit val timeout = Timeout(10, TimeUnit.SECONDS)
    implicit val materializer = adapter.materializer

    "handle get all screenboards" in {
      val res = Await.result(client.getAllScreenboards, Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/screen?api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }

    "handle add screenboard" in {
      val res = Await.result(client.addScreenboard("POOP"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/screen?api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.entity.asString == "POOP")

      adapter.getRequest must beSome.which(_.method == HttpMethods.POST)
    }

    "handle get screenboard" in {
      val res = Await.result(client.getScreenboard(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/screen/12345?api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }

    "handle delete screenboard" in {
      val res = Await.result(client.deleteScreenboard(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/screen/12345?api_key=apiKey&application_key=appKey")

      adapter.getRequest must beSome.which(_.method == HttpMethods.DELETE)
    }

    "handle update screenboard" in {
      val res = Await.result(client.updateScreenboard(12345, "POOP"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/screen/12345?api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.entity.asString == "POOP")

      adapter.getRequest must beSome.which(_.method == HttpMethods.PUT)
    }
  }
}