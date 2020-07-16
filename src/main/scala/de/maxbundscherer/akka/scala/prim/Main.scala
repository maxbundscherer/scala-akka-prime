package de.maxbundscherer.akka.scala.prim

object Main extends App {

  import de.maxbundscherer.akka.scala.prim.services.ActorSystemService
  import de.maxbundscherer.akka.scala.prim.utils.LCMCalculator
  import de.maxbundscherer.akka.scala.prim.utils.JobController

  private val actorSystemService = new ActorSystemService()

  private val resultFilename: String        = "results.csv"

  private val repeatRun: Int                = 5
  private val maxWorkersPerRun: Vector[Int] = Vector(1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22)

  private val to: Int = LCMCalculator.lcmOfArray(maxWorkersPerRun.toArray).toInt

  JobController.startJob(
    actorSystemService  = actorSystemService,
    maxWorkersPerRun    = maxWorkersPerRun,
    repeatRun           = repeatRun,
    to                  = to,
    resultFilename      = resultFilename
  )

}