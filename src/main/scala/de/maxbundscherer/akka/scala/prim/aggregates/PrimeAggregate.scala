package de.maxbundscherer.akka.scala.prim.aggregates

object PrimeAggregate {

  trait Request
  trait State

  final case class RangeSpec(from: Int,
                             to: Int)

}
