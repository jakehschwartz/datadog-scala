package com.jakehschwartz

import akka.http.scaladsl.model.ResponseEntity
import akka.stream.Materializer
import akka.util.Timeout

import scala.concurrent.Await

package object datadog {

  case class Metric(
                     name: String,
                     points: Seq[(Long, Double)],
                     metricType: Option[String] = None,
                     tags: Option[Seq[String]] = None,
                     host: Option[String]
                   )

  case class Response(statusCode: Int, body: String)

  implicit class DatadogResponseEntity(val entity: ResponseEntity)(implicit timeout: Timeout, materializer: Materializer) {
    def asString = Await.result(entity.toStrict(timeout.duration), timeout.duration).data.utf8String
  }
}