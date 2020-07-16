package de.maxbundscherer.akka.scala.prim.actors

import de.maxbundscherer.akka.scala.prim.utils.CSV

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior

object SupervisorActor extends CSV {

  import de.maxbundscherer.akka.scala.prim.aggregates.PrimeAggregate._

  final case class StartJobCmd(
                                to: Int,
                                maxWorkers: Int,
                                resultFilename: String
                                ) extends Request

  final case class ProcessResultCmd(rangeSpec: RangeSpec,
                                    primes: Vector[Int]) extends Request

  private final case class RunningState(
                                        unprocessedRanges: Vector[RangeSpec],
                                        processedRanges: Map[RangeSpec, Vector[Int]],
                                        startTime: Long,
                                        cmd: StartJobCmd) extends State

  private final case class FinishedState(
                                          primes: Vector[Int],
                                          startTime: Long,
                                          endTime: Long,
                                          cmd: StartJobCmd
                                          ) extends State

  def apply(): Behavior[Request] = applyIdle()

  /**
   * State Idle
   */
  private def applyIdle(): Behavior[Request] = Behaviors.receive { (context, message) =>

    message match {

      case cmd: StartJobCmd =>

        context.log.info(s"Should start job ($cmd)")

        if(cmd.to % cmd.maxWorkers != 0) throw new RuntimeException("Can't split range correctly")

        val step: Int = cmd.to / cmd.maxWorkers

        val subRanges: Seq[RangeSpec] = for (i <- 0 until cmd.maxWorkers) yield {
          val from = i * step + 1
          RangeSpec(from = from, to = from + step - 1)
        }

        context.log.debug(s"Got ${subRanges.size} subRanges (${subRanges.toVector.toString()})")

        subRanges.foreach(r => {
          val worker = context.spawn( WorkerActor(), s"worker-${r.from}-${r.to}")
          worker ! WorkerActor.CalcRangeCmd(r, context.self)
        })

        applyRunning(RunningState(
          unprocessedRanges = subRanges.toVector,
          processedRanges = Map.empty,
          startTime = System.nanoTime(),
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
          processedRanges   = state.processedRanges + (cmd.rangeSpec -> cmd.primes)
        )

        if(newState.unprocessedRanges.isEmpty) {
          applyFinished(FinishedState(
            primes = newState.processedRanges.values.toVector.flatten.sorted,
            startTime = newState.startTime,
            endTime = System.nanoTime(),
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

    CSVWriter.writeToCSV(
      to = state.cmd.to,
      maxWorkers = state.cmd.maxWorkers,
      time = state.endTime - state.startTime,
      primeCounter = state.primes.size,
      filename = state.cmd.resultFilename
    )

    //Terminate actor system (future binding on whenTerminated)
    context.system.terminate()
    Behaviors.same
  }

}