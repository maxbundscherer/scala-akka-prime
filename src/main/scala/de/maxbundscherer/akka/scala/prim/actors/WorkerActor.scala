package de.maxbundscherer.akka.scala.prim.actors

object WorkerActor {

  import de.maxbundscherer.akka.scala.prim.aggregates.PrimeAggregate._
  import de.maxbundscherer.akka.scala.prim.utils.Calculator

  import akka.actor.typed.scaladsl.Behaviors
  import akka.actor.typed.{ActorRef, Behavior}

  final case class CalcRangeCmd(rangeSpec: RangeSpec,
                                replyTo: ActorRef[Request]
                               ) extends Request

  def apply(): Behavior[Request] = Behaviors.receive { (context, message) =>

    message match {

      case cmd: CalcRangeCmd =>

        context.log.debug(s"Should calc range (${cmd.rangeSpec})")

        cmd.replyTo ! SupervisorActor.ProcessResultCmd(
          rangeSpec = cmd.rangeSpec,
          primes = (cmd.rangeSpec.from to cmd.rangeSpec.to).filter(i => Calculator.isPrime(i)).toVector
        )

    }

    Behaviors.same
  }

}