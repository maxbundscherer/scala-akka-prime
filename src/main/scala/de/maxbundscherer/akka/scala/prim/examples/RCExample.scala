package de.maxbundscherer.akka.scala.prim.examples

object SupervisorActor {

  import akka.actor.typed.scaladsl.Behaviors
  import akka.actor.typed.Behavior
  import akka.actor.typed.ActorRef

  //Declare request wrapper and internal state
  sealed trait Request
  private case class State(value: Int)

  case class TriggerRunCmd()                          extends Request
  case class ProcessNewValueCmd(value: Int)           extends Request
  case class GetValueCmd(replyTo: ActorRef[Request])  extends Request

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

        w1 ! WorkerActor.StartTimerCmd(replyTo = context.self)
        w2 ! WorkerActor.StartTimerCmd(replyTo = context.self)

        Behaviors.same

      case cmd: GetValueCmd =>

        cmd.replyTo ! WorkerActor.ProcessNewValueCmd(value = state.value, replyTo = context.self)

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
  import scala.concurrent.duration._
  import scala.language.postfixOps

  private case class ActiveState(replyTo: ActorRef[SupervisorActor.Request])

  private case class TimerKeyCmd() extends SupervisorActor.Request

  case class StartTimerCmd(replyTo: ActorRef[SupervisorActor.Request]) extends SupervisorActor.Request
  case class ProcessNewValueCmd(value: Int, replyTo: ActorRef[SupervisorActor.Request]) extends SupervisorActor.Request

  def apply(): Behavior[SupervisorActor.Request] = applyIdle()

  def applyIdle(): Behavior[SupervisorActor.Request] = Behaviors.receive { (context, message) =>

    message match {

      case cmd: StartTimerCmd =>

        context.log.info("Start periodic timer")
        applyActive(ActiveState(cmd.replyTo))

    }

  }

  def applyActive(state: ActiveState): Behavior[SupervisorActor.Request] = Behaviors.withTimers[SupervisorActor.Request] {
    timers =>
      timers.startTimerAtFixedRate(msg = TimerKeyCmd(), interval =  500 milliseconds)

      Behaviors.receive{ (context, message) =>

        message match {

          case _: TimerKeyCmd =>

            context.log.debug("Got trigger from AS")
            state.replyTo ! SupervisorActor.GetValueCmd(replyTo = context.self)

          case cmd: ProcessNewValueCmd =>

            context.log.debug(s"Got data ${cmd.value}")
            cmd.replyTo ! SupervisorActor.ProcessNewValueCmd(cmd.value + 1)

        }

        Behaviors.same

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
