package de.maxbundscherer.akka.scala.prim.examples

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import akka.actor.typed.ActorSystem

object TestActor {

  //Declare request wrapper and internal state
  sealed trait Request
  private case class State(accountBalance: Int)

  //Declare concrete request
  case class IncreaseBalance(amount: Int) extends Request

  //Default state is idle (define internal state)
  def apply(): Behavior[Request] = applyIdle(State(accountBalance = 0))

  //Process messages in state idle
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

  //Init actor system and actor
  private val actorSystem: ActorSystem[TestActor.Request]  = ActorSystem(TestActor(), "actorSystem")

  //Fire and forget two requests
  actorSystem ! TestActor.IncreaseBalance(amount = 10)
  actorSystem ! TestActor.IncreaseBalance(amount = -5)

}
