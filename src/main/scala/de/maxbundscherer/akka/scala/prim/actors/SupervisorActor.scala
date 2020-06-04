package de.maxbundscherer.akka.scala.prim.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object SupervisorActor {

  import de.maxbundscherer.akka.scala.prim.aggregates.PrimeAggregate._

  final case class StartJobCmd(rangeSpec: RangeSpec,
                               maxWorkers: Int) extends Request

  final case class ProcessResultCmd(rangeSpec: RangeSpec,
                                    primes: Vector[Int]) extends Request

  private final case class RunningState(unprocessedRanges: Vector[RangeSpec],
                                  processedRanges: Map[RangeSpec, Vector[Int]],
                                  cmd: StartJobCmd) extends State

  private final case class FinishedState(primes: Vector[Int],
                                  cmd: StartJobCmd) extends State

  def apply(): Behavior[Request] = applyIdle()

  /**
   * State Idle
   */
  private def applyIdle(): Behavior[Request] = Behaviors.receive { (context, message) =>

    message match {

      case cmd: StartJobCmd =>

        context.log.info(s"Should start job ($cmd)")

        //TODO: Parallel
        val w1 = context.spawn( WorkerActor(), "worker1" )
        w1 ! WorkerActor.CalcRangeCmd(cmd.rangeSpec, context.self)

        applyRunning(RunningState(
          unprocessedRanges = Vector(cmd.rangeSpec),
          processedRanges = Map.empty,
          cmd = cmd
        ))

    }

  }

  /**
   * State Running
   */
  private def applyRunning(state: RunningState): Behavior[Request] = Behaviors.receive { (context, message) =>

    message match {

      case cmd: ProcessResultCmd =>

        val newState = state.copy(
          unprocessedRanges = state.unprocessedRanges.filter(i => i != cmd.rangeSpec),
          processedRanges = state.processedRanges + (cmd.rangeSpec -> cmd.primes)
        )

        if(newState.unprocessedRanges.isEmpty) {
          applyFinished(FinishedState(
            primes = state.processedRanges.values.toVector.flatten,
            cmd = state.cmd
          ))
        }
        else applyRunning(newState)

    }

  }

  /**
   * State Finished
   */
  private def applyFinished(state: FinishedState): Behavior[Request] = Behaviors.setup { context =>

    context.log.info(s"Finished! ($state)")

    Behaviors.same
  }

}