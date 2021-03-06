package com.jakehschwartz.datadog

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri._
import akka.http.scaladsl.model._
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging

import java.util.concurrent.TimeUnit
import scala.concurrent.Future

class HttpAdapter(
                   httpTimeoutSeconds: Long = 10L,
                   actorSystem: Option[ActorSystem] = None
                 ) extends LazyLogging {

  // If we didn't get an actor system passed in
  implicit val system = actorSystem.getOrElse(ActorSystem())
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  // Akka's Ask pattern requires an implicit timeout to know
  // how long to wait for a response.
  implicit val timeout = Timeout(httpTimeoutSeconds, TimeUnit.SECONDS)

  def doRequest(
                 scheme: String,
                 authority: String,
                 path: String,
                 method: String,
                 body: Option[String] = None,
                 params: Map[String, Option[String]] = Map.empty,
                 contentType: String = "json"
               ): Future[Response] = {

    // Turn a map of string,opt[string] into a map of string,string which is
    // what Query wants
    val filteredParams = params.flatMap { case (k, vOpt) =>
      vOpt.map(k -> _)
    }
    // Make a Uri
    val finalUrl = Uri(
      scheme = scheme,
      authority = Authority(host = Host(authority)),
      path = Path("/api/v1/" + path),
      queryString = Option(Query(filteredParams).toString())
    )

    // Use the provided case classes from spray-client
    // to construct an HTTP request of the type needed.
    val httpRequest: HttpRequest = method match {
      case "DELETE" => HttpRequest(uri = finalUrl, method = HttpMethods.DELETE, entity = body.fold(HttpEntity.Empty)(HttpEntity(_)))
      case "GET" => HttpRequest(uri = finalUrl, method = HttpMethods.GET, entity = body.fold(HttpEntity.Empty)(HttpEntity(_)))
      case "POST" => contentType match {
        case "json" => HttpRequest(uri = finalUrl, method = HttpMethods.POST, entity = body.fold(HttpEntity.Empty)(HttpEntity(ContentTypes.`application/json`, _)))
        case _ =>
          // This is going to be a form-encoded post. There's only one
          // API call that works this way (ugh) so I'm not going to worry
          // too much about making this work as cleanly as the rest of the
          // stuff. (IMO)
          val formUrl = Uri(
            scheme = scheme,
            authority = Authority(host = Host(authority)),
            path = Path("/api/v1/" + path)
          )

          HttpRequest(uri = formUrl, method = HttpMethods.POST, entity = FormData(filteredParams).toEntity)
      }
      case "PUT" => HttpRequest(uri = finalUrl, method = HttpMethods.PUT, entity = body.fold(HttpEntity.Empty)(HttpEntity(ContentTypes.`application/json`, _)))
      case _ => throw new IllegalArgumentException("Unknown HTTP method: " + method)
    }

    logger.debug("%s: %s".format(method, finalUrl))
    // For spelunkers, the ? is a function of the Akka "ask pattern". Unlike !
    // it waits for a response in the form of a future. In this case we're
    // sending along a case class representing the type of HTTP request we want
    // to do and something down in the guts of the actors handles it and gets
    // us a response.
    doHttp(httpRequest)
  }

  def doHttp(request: HttpRequest): Future[Response] = {
    Http().singleRequest(request).map { res =>
      Response(res.status.intValue, res.entity.asString)
    }
  }

  def shutdown = {
    system.terminate()
  }
}
