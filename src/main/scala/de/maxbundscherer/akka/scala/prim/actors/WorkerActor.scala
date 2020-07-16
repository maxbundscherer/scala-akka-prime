package de.maxbundscherer.akka.scala.prim.actors

object WorkerActor {

  import de.maxbundscherer.akka.scala.prim.aggregates.PrimeAggregate._
  import de.maxbundscherer.akka.scala.prim.utils.Calculator

  import akka.actor.typed.scaladsl.Behaviors
  import akka.actor.typed.{ActorRef, Behavior}

  final case class ProcessRangeCmd(
                                    range: RangeSpec,
                                    replyTo: ActorRef[Request]
                                  ) extends Request

  /**
   * State Idle
   */
  def apply(): Behavior[Request] = Behaviors.receive { (context, message) =>

    message match {

      case cmd: ProcessRangeCmd =>

        context.log.debug(s"Process range (${cmd.range})")

        cmd.replyTo ! SupervisorActor.ProcessRangeResultsCmd(
          range   = cmd.range,
          results = (cmd.range.from to cmd.range.to).filter(i => Calculator.isPrime(i)).toVector
        )

    }

    Behaviors.stopped
  }

}