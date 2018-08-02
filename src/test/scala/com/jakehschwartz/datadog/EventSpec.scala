package com.jakehschwartz.datadog

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model.HttpMethods
import akka.util.Timeout
import org.json4s._
import org.json4s.native.JsonMethods._
import org.specs2.mutable.Specification

import scala.concurrent.Await
import scala.concurrent.duration._

class EventSpec extends Specification {

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

    "handle get event" in {
      val res = Await.result(client.getEvent(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/events/12345?api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }

    "handle add event" in {
      val res = Await.result(client.addEvent(title = "poop", text = "fart"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/events?api_key=apiKey&application_key=appKey")
      val body = parse(adapter.getRequest.get.entity.asString)
      (body \ "title").extract[String] must beEqualTo("poop")
      (body \ "text").extract[String] must beEqualTo("fart")

      adapter.getRequest must beSome.which(_.method == HttpMethods.POST)
    }

    "handle get events" in {
      val res = Await.result(client.getEvents(start = 12345, end = 12346), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      val uri = adapter.getRequest.get.uri.toString
      uri must contain("https://app.datadoghq.com/api/v1/events")
      uri must contain("end=12346")
      uri must contain("start=12345")
      uri must contain("api_key=apiKey")
      uri must contain("application_key=appKey")
    }

    "handle delete events" in {
      val res = Await.result(client.deleteEvent(eventId = 123456), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      val uri = adapter.getRequest.get.uri.toString
      uri must contain("https://app.datadoghq.com/api/v1/events/123456")
      uri must contain("api_key=apiKey")
      uri must contain("application_key=appKey")
    }
  }
}
