package de.maxbundscherer.akka.scala.prim

object Main extends App {

  import de.maxbundscherer.akka.scala.prim.utils.Calculator
  import de.maxbundscherer.akka.scala.prim.services.ActorSystemService
  import de.maxbundscherer.akka.scala.prim.utils.JobController

  private val actorSystemService = new ActorSystemService()

  private val resultFilename: String        = "results.csv"

  private val repeatRun: Int                = 5
  private val maxWorkersPerRun: Vector[Int] = Vector(1, 2, 3, 4, 5, 6, 7, 8)

  private val to = Calculator.calcTo(maxWorkersPerRun)

  JobController.startJob(
    actorSystemService  = actorSystemService,
    maxWorkersPerRun    = maxWorkersPerRun,
    repeatRun           = repeatRun,
    to                  = to,
    resultFilename      = resultFilename
  )

}