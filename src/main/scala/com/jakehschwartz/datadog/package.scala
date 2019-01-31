package com.jakehschwartz

import akka.http.scaladsl.model.ResponseEntity
import akka.stream.Materializer
import akka.util.Timeout

import scala.concurrent.Await

package object datadog {
  implicit class DatadogResponseEntity(val entity: ResponseEntity)(implicit timeout: Timeout, materializer: Materializer) {
    def asString = Await.result(entity.toStrict(timeout.duration), timeout.duration).data.utf8String
  }
}