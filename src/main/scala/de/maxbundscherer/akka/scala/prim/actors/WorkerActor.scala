package de.maxbundscherer.akka.scala.prim.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object WorkerActor {

  import de.maxbundscherer.akka.scala.prim.aggregates.PrimeAggregate._

  final case class CalcRangeCmd(rangeSpec: RangeSpec,
                                replyTo: ActorRef[Request]
                               ) extends Request

  private def isPrime(n: Int): Boolean = ! ((2 until n-1) exists (n % _ == 0))

  def apply(): Behavior[Request] = Behaviors.receive { (context, message) =>

    message match {

      case cmd: CalcRangeCmd =>

        context.log.debug(s"Should calc range (${cmd.rangeSpec})")

        cmd.replyTo ! SupervisorActor.ProcessResultCmd(
          rangeSpec = cmd.rangeSpec,
          primes = (cmd.rangeSpec.from to cmd.rangeSpec.to).filter(i => isPrime(i)).toVector
        )

    }

    Behaviors.same
  }

}