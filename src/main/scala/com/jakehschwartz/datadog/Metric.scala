package com.jakehschwartz.datadog

/**
  * Created by Jake on 2019-01-30.
  */
final case class Metric(
                         name: String,
                         points: Seq[(Long, Double)],
                         metricType: Option[String] = None,
                         tags: Option[Seq[String]] = None,
                         host: Option[String]
                       )
