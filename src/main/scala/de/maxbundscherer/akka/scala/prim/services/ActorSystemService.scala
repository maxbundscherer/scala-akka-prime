package de.maxbundscherer.akka.scala.prim.services

import akka.actor.typed.ActorSystem
import de.maxbundscherer.akka.scala.prim.actors.SupervisorActor
import de.maxbundscherer.akka.scala.prim.aggregates.PrimeAggregate._

class ActorSystemService {

  private val actorSystem: ActorSystem[Request]  = ActorSystem(SupervisorActor(), "actorSystem")

  def startJob(from: Int, to: Int, maxWorkers: Int): Unit = {

    actorSystem ! SupervisorActor.StartJobCmd(
      rangeSpec = RangeSpec(0, 100),
      maxWorkers = maxWorkers
    )

  }

}
