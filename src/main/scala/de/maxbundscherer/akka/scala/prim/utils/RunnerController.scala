package de.maxbundscherer.akka.scala.prim.utils

object RunnerController {

  import de.maxbundscherer.akka.scala.prim.services.ActorSystemService

  import scala.collection.mutable
  import scala.concurrent.duration.Duration
  import scala.concurrent.{Await, Future}
  import akka.Done

  /**
   * Do run
   * @param actorSystemService ActorSystemService
   * @param maxRepeats Number of repeats per maxWorker
   * @param to e.g. 100
   * @param maxWorkers e.g. 5
   * @param resultFilename e.g. result.csv
   */
  def doRun(
             actorSystemService: ActorSystemService,
             maxRepeats: Int,
             to: Int,
             maxWorkers: Vector[Int],
             resultFilename: String
           ): Unit = {

    val maxWorkersStack: mutable.Stack[Int] = new mutable.Stack()
    maxWorkers.reverse.foreach(i => maxWorkersStack.push(i))

    while(maxWorkersStack.nonEmpty) {

      val currentMaxWorker: Int = maxWorkersStack.pop()
      var iRepeat: Int          = 0

      while(iRepeat < maxRepeats) {

        val future: Future[Done] = actorSystemService.startJob(
          to = to,
          maxWorkers = currentMaxWorker,
          resultFilename = resultFilename
        )

        Await.result(future, atMost = Duration.Inf)
        iRepeat = iRepeat + 1
      }

    }

  }

}