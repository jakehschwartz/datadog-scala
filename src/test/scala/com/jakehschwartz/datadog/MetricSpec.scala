package com.jakehschwartz.datadog

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model.HttpMethods
import akka.util.Timeout
import org.json4s._
import org.json4s.native.JsonMethods._
import org.specs2.mutable.Specification

import scala.concurrent.Await
import scala.concurrent.duration._

class MetricSpec extends Specification {

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

    "handle add metrics" in {
      val res = Await.result(client.addMetrics(
        series = Seq(
          Metric(
            name = "foo.bar.test",
            points = Seq((1412183578, 12.0), (1412183579, 123.0)),
            host = Some("poop.example.com"),
            tags = Some(Seq("tag1", "tag2:foo")),
            metricType = Some("gauge")
          ),
          Metric(
            name = "foo.bar.gorch",
            points = Seq((1412183580, 12.0), (1412183581, 123.0)),
            host = Some("poop2.example.com"),
            tags = Some(Seq("tag3", "tag3:foo")),
            metricType = Some("counter")
          )
        )
      ), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/series?api_key=apiKey&application_key=appKey")
      val body = parse(adapter.getRequest.get.entity.asString)
      val series = body.asInstanceOf[JObject].obj.head._2.asInstanceOf[JArray].arr.map(_.asInstanceOf[JObject])
      val names = series.flatMap(_.obj.collect {
        case (key, value) if key == "metric" => value.asInstanceOf[JString].s
      })

      names must have size 2
      names must contain(allOf("foo.bar.test", "foo.bar.gorch"))

      val points = series.flatMap(_.obj.collect {
        case (key, value) if key == "points" => value.asInstanceOf[JArray].arr
      })

      points must have size 2
      val entry1: Seq[JValue] = Seq(JArray(List(JInt(1412183578), JDouble(12.0))), JArray(List(JInt(1412183579), JDouble(123.0))))
      val entry2: Seq[JValue] = Seq(JArray(List(JInt(1412183580), JDouble(12.0))), JArray(List(JInt(1412183581), JDouble(123.0))))
      points must contain(allOf(entry1, entry2))

      adapter.getRequest must beSome.which(_.method == HttpMethods.POST)
    }

    "handle query timeseries" in {
      val res = Await.result(client.query(
        query = "system.cpu.idle{*}by{host}",
        from = 1470453155,
        to = 1470539518
      ), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      val params = adapter.getRequest.get.uri.query().toMap

      params must havePairs(
        "api_key" -> "apiKey",
        "application_key" -> "appKey",
        "query" -> "system.cpu.idle{*}by{host}",
        "from" -> "1470453155",
        "to" -> "1470539518"
      )

      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }
  }
}
