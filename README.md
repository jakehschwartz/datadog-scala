[![CircleCI](https://circleci.com/gh/jakehschwartz/datadog-scala/tree/master.svg?style=svg)](https://circleci.com/gh/jakehschwartz/datadog-scala/tree/master)

# Datadog-Scala

A Scala library for interacting with the Datadog API.

As of October 2014 this library covers all the methods in the [Datadog API Documentation](http://docs.datadoghq.com/api/).

# Example

```scala
import com.jakehschwartz.datadog.Client
import scala.concurrent.ExecutionContext.Implicits.global

val client = new Client(apiKey = "XXX", appKey = "XXX")
client.getAllTimeboards.foreach({ response =>
    println(response.body)
})
```

# Using It

This library is available on Maven Central and is only built for Scala 2.12.

```
libraryDependencies += "com.jakehschwartz.datadog" %% "datadog-scala" % "1.4.0"
```
