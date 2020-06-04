package de.maxbundscherer.akka.scala.prim

import de.maxbundscherer.akka.scala.prim.services.ActorSystemService

object Main extends App {

  val actorSystemService = new ActorSystemService()

  actorSystemService.startJob(
    from = 0,
    to = 100,
    maxWorkers = 1
  )

}
