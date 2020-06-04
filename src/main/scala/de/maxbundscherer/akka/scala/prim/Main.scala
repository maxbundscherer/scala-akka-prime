package de.maxbundscherer.akka.scala.prim

import de.maxbundscherer.akka.scala.prim.services.ActorSystemService

object Main extends App {

  val actorSystemService = new ActorSystemService()

  actorSystemService.startJob(
    to = 100,
    maxWorkers = 2
  )

}
