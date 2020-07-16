package de.maxbundscherer.akka.scala.prim

object Main extends App {

  import de.maxbundscherer.akka.scala.prim.utils.Calculator
  import de.maxbundscherer.akka.scala.prim.services.ActorSystemService
  import de.maxbundscherer.akka.scala.prim.utils.RunnerController

  private val actorSystemService = new ActorSystemService()

  private val maxRepeats: Int         = 5
  private val resultFilename: String  = "result.csv"
  private val maxWorkers: Vector[Int] = Vector(1, 2, 3, 4, 5, 6, 7, 8)

  private val to = Calculator.calcTo(maxWorkers) * 10

  RunnerController.doRun(
    actorSystemService = actorSystemService,
    maxRepeats = maxRepeats,
    to = to,
    maxWorkers = maxWorkers,
    resultFilename = resultFilename
  )

}