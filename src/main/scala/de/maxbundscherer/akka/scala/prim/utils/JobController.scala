package de.maxbundscherer.akka.scala.prim.utils

object JobController {

  import de.maxbundscherer.akka.scala.prim.services.ActorSystemService

  import scala.collection.mutable
  import scala.concurrent.duration.Duration
  import scala.concurrent.{Await, Future}
  import akka.Done

  /**
   * Start Job
   * @param actorSystemService ActorSystemService
   * @param maxWorkersPerRun Number of maxWorkers per Run
   * @param repeatRun Repeats Run
   * @param to e.g. 100
   * @param resultFilename e.g. result.csv
   */
  def startJob(
             actorSystemService: ActorSystemService,
             maxWorkersPerRun: Vector[Int],
             repeatRun: Int,
             to: Int,
             resultFilename: String
           ): Unit = {

    //Build stack
    val maxWorkersPerRunStack: mutable.Stack[Int] = new mutable.Stack()
    maxWorkersPerRun.reverse.foreach(i => maxWorkersPerRunStack.push(i))

    while(maxWorkersPerRunStack.nonEmpty) {

      val currentMaxWorker: Int = maxWorkersPerRunStack.pop()
      var iRepeat: Int          = 0

      while(iRepeat < repeatRun) {

        val future: Future[Done] = actorSystemService.startRun(
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