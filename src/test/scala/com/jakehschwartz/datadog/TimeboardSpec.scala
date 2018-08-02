package com.jakehschwartz.datadog

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model.HttpMethods
import akka.util.Timeout
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

    implicit val timeout = Timeout(10, TimeUnit.SECONDS)
    implicit val materializer = adapter.materializer

    "handle get all timeboards" in {
      val res = Await.result(client.getAllTimeboards, Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/dash?api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }

    "handle add timeboard" in {
      val res = Await.result(client.addTimeboard("POOP"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/dash?api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.entity.asString == "POOP")

      adapter.getRequest must beSome.which(_.method == HttpMethods.POST)
    }

    "handle get timeboard" in {
      val res = Await.result(client.getTimeboard(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/dash/12345?api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }

    "handle delete timeboard" in {
      val res = Await.result(client.deleteTimeboard(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/dash/12345?api_key=apiKey&application_key=appKey")

      adapter.getRequest must beSome.which(_.method == HttpMethods.DELETE)
    }

    "handle update timeboard" in {
      val res = Await.result(client.updateTimeboard(12345, "POOP"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/dash/12345?api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.entity.asString == "POOP")

      adapter.getRequest must beSome.which(_.method == HttpMethods.PUT)
    }
  }
}