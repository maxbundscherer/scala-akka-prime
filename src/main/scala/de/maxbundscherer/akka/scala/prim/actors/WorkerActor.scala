package de.maxbundscherer.akka.scala.prim.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object WorkerActor {

  import de.maxbundscherer.akka.scala.prim.aggregates.PrimeAggregate._

  final case class CalcRangeCmd(rangeSpec: RangeSpec,
                                replyTo: ActorRef[Request]
                               ) extends Request

  def apply(): Behavior[Request] = Behaviors.receive { (context, message) =>

    message match {

      case cmd: CalcRangeCmd =>

        context.log.info(s"Should calc range ($cmd)")

        cmd.replyTo ! SupervisorActor.ProcessResultCmd(
          rangeSpec = cmd.rangeSpec,
          primes = Vector(cmd.rangeSpec.from, cmd.rangeSpec.to) //TODO: Implement isPrime
        )

    }

    Behaviors.same
  }

}