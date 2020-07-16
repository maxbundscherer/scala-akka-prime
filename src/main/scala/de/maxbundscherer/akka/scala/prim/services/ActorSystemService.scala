package de.maxbundscherer.akka.scala.prim.services

class ActorSystemService {

  import de.maxbundscherer.akka.scala.prim.actors.SupervisorActor
  import de.maxbundscherer.akka.scala.prim.aggregates.PrimeAggregate._

  import akka.Done
  import akka.actor.typed.ActorSystem
  import scala.concurrent.Future

  /**
   * Start Job
   * @param to e.g. 100
   * @param maxWorkers e.g. 2
   * @param resultFilename e.g. result.csv
   * @return Future (binding on whenTerminated)
   */
  def startJob(to: Int, maxWorkers: Int, resultFilename: String): Future[Done] = {

    val actorSystem: ActorSystem[Request]  = ActorSystem(SupervisorActor(), "actorSystem")

    actorSystem ! SupervisorActor.StartJobCmd(
      to = to,
      maxWorkers = maxWorkers,
      resultFilename = resultFilename
    )

    actorSystem.whenTerminated
  }

}