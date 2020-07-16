package de.maxbundscherer.akka.scala.prim.actors

import de.maxbundscherer.akka.scala.prim.utils.CSV

object SupervisorActor extends CSV {

  import de.maxbundscherer.akka.scala.prim.aggregates.PrimeAggregate._

  import akka.actor.typed.scaladsl.Behaviors
  import akka.actor.typed.Behavior

  final case class StartRunCmd(
                                to: Int,
                                maxWorkers: Int,
                                resultFilename: String
                                ) extends Request

  final case class ProcessRangeResultsCmd(
                                            range: RangeSpec,
                                            results: Vector[Int]
                                         ) extends Request

  private final case class ProcessingState(
                                        unprocessedRanges: Vector[RangeSpec],
                                        processedRanges: Map[RangeSpec, Vector[Int]],
                                        startTime: Long,
                                        startRunCmd: StartRunCmd
                                          ) extends State

  private final case class FinishedState(
                                          results: Vector[Int],
                                          startTime: Long,
                                          endTime: Long,
                                          startRunCmd: StartRunCmd
                                          ) extends State

  def apply(): Behavior[Request] = applyIdle()

  /**
   * State Idle
   */
  private def applyIdle(): Behavior[Request] = Behaviors.receive { (context, message) =>

    message match {

      case cmd: StartRunCmd =>

        context.log.info(s"Start run ($cmd)")

        //Check if can build subRanges correctly
        if(cmd.to % cmd.maxWorkers != 0) throw new RuntimeException("Can't split range correctly")

        //Calc subRanges
        val step: Int = cmd.to / cmd.maxWorkers

        val subRanges: Seq[RangeSpec] = for (i <- 0 until cmd.maxWorkers) yield {
          val from = i * step + 1
          RangeSpec(from = from, to = from + step - 1)
        }

        context.log.debug(s"Got ${subRanges.size} subRanges")

        //Start for each subRange a worker
        subRanges.foreach(subRange => {
          val worker = context.spawn(WorkerActor(), name = s"worker-${subRange.from}-${subRange.to}")
          worker ! WorkerActor.ProcessRangeCmd(range = subRange, replyTo = context.self)
        })

        applyProcessing(ProcessingState(
          unprocessedRanges = subRanges.toVector,
          processedRanges = Map.empty,
          startTime = System.nanoTime(),
          startRunCmd = cmd
        ))

    }

  }

  /**
   * State Processing
   */
  private def applyProcessing(state: ProcessingState): Behavior[Request] = Behaviors.receive { (context, message) =>

    message match {

      case cmd: ProcessRangeResultsCmd =>

        val newState = state.copy(
          unprocessedRanges = state.unprocessedRanges.filter(i => i != cmd.range),
          processedRanges   = state.processedRanges + (cmd.range -> cmd.results)
        )

        if(newState.unprocessedRanges.isEmpty) {
          applyFinished(FinishedState(
            results = newState.processedRanges.values.toVector.flatten.sorted,
            startTime = newState.startTime,
            endTime = System.nanoTime(),
            startRunCmd = state.startRunCmd
          ))
        }
        else applyProcessing(newState)

    }

  }

  /**
   * State Finished
   */
  private def applyFinished(state: FinishedState): Behavior[Request] = Behaviors.setup { context =>

    context.log.info(s"Start run (${state.startRunCmd})")

    CSVWriter.writeResultsToCSV(
      to          = state.startRunCmd.to,
      maxWorkers  = state.startRunCmd.maxWorkers,
      time        = state.endTime - state.startTime,
      resultsSize = state.results.size,
      startTime   = state.startTime,
      filename    = state.startRunCmd.resultFilename
    )

    //Terminate actor system (future binding on whenTerminated)
    context.system.terminate()
    Behaviors.stopped
  }

}