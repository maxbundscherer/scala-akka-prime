package de.maxbundscherer.akka.scala.prim.examples

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.ActorSystem

object TestActor {

  sealed trait Request

  private case class State(accountBalance: Int)

  case class IncreaseBalance(amount: Int) extends Request

  //Default state is idle
  def apply(): Behavior[Request] = applyIdle(State(accountBalance = 0))

  //State Idle (process messages)
  private def applyIdle(state: State): Behavior[Request] = Behaviors.receive { (context, message) =>

    message match {

      case cmd: IncreaseBalance =>

        val newState: State = state.copy(accountBalance = state.accountBalance + cmd.amount)
        context.log.info(s"Old balance was (${state.accountBalance}) / New balance is (${newState.accountBalance})")
        applyIdle(newState)

    }

  }

}

object MainExample extends App {

  //Init actor system
  private val actorSystem: ActorSystem[TestActor.Request]  = ActorSystem(TestActor(), "actorSystem")

  //Fire and forget
  actorSystem ! TestActor.IncreaseBalance(amount = 10)
  actorSystem ! TestActor.IncreaseBalance(amount = -5)

}
