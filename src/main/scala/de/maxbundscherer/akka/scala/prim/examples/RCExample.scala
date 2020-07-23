package de.maxbundscherer.akka.scala.prim.examples

object SupervisorActor {

  import akka.actor.typed.scaladsl.Behaviors
  import akka.actor.typed.Behavior

  //Declare request wrapper and internal state
  sealed trait Request
  private case class State(value: Int)

  case class TriggerRunCmd()                extends Request
  case class ProcessNewValueCmd(value: Int) extends Request

  def apply(): Behavior[Request] = applyIdle(State(value = 0))

  /**
   * State Idle
   */
  private def applyIdle(state: State): Behavior[Request] = Behaviors.receive { (context, message) =>

    message match {

      case _: TriggerRunCmd =>

        context.log.info(s"Start run with initial $state")

        val w1 = context.spawn(WorkerActor(), name = s"worker-1")
        val w2 = context.spawn(WorkerActor(), name = s"worker-2")

        w1 ! WorkerActor.IncrementValueCmd(value = state.value, replyTo = context.self)
        w2 ! WorkerActor.IncrementValueCmd(value = state.value, replyTo = context.self)

        Behaviors.same

      case cmd: ProcessNewValueCmd =>

        val oldState = state
        val newState = oldState.copy(value = cmd.value)

        context.log.info(s"OldState $state / NewState $state")

        applyIdle(newState)

    }

  }

}

object WorkerActor {

  import akka.actor.typed.scaladsl.Behaviors
  import akka.actor.typed.Behavior
  import akka.actor.typed.ActorRef

  case class IncrementValueCmd(value: Int, replyTo: ActorRef[SupervisorActor.Request]) extends SupervisorActor.Request

  def apply(): Behavior[SupervisorActor.Request] = Behaviors.receive { (context, message) =>

    message match {

      case cmd: IncrementValueCmd =>

        context.log.info(s"Should increment (${cmd.value}) from SupervisorActor")
        cmd.replyTo ! SupervisorActor.ProcessNewValueCmd(value = cmd.value + 1)
        Behaviors.stopped

    }

  }

}

//Add 'extends App' to object 'RCExample' to run rcExample
object RCExample {

  import akka.actor.typed.ActorSystem

  //Init actor system and actor
  private val actorSystem: ActorSystem[SupervisorActor.Request]  = ActorSystem(SupervisorActor(), "actorSystem")

  //Fire and forget two requests
  actorSystem ! SupervisorActor.TriggerRunCmd()

}
