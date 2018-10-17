package org.tmt.wfos.ProbeArmhcd

import akka.actor.typed.scaladsl.{Behaviors, MutableBehavior, TimerScheduler}
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import csw.framework.CurrentStatePublisher
import csw.messages.framework.ComponentInfo
import csw.messages.params.generics.KeyType.IntKey
import csw.messages.params.states.{CurrentState, StateName}
import csw.services.logging.scaladsl.LoggerFactory
import org.tmt.wfos.ProbeArmhcd.ProbearmHcdMessage.{Move, Tick}

import scala.concurrent.duration.DurationDouble

class ProbearmHcdWorker(ctx: ActorContext[ProbearmHcdMessage],
                        timer: TimerScheduler[ProbearmHcdMessage],
                        currentStatePublisher: CurrentStatePublisher,
                        componentInfo: ComponentInfo,
                        loggerFactory: LoggerFactory)
    extends MutableBehavior[ProbearmHcdMessage] {

  private val logger = loggerFactory.getLogger(ctx)
  timer.startPeriodicTimer("publish", Tick, 1000.millis)

  var moveX    = 0
  var moveY    = 0
  var currentX = 0
  var currentY = 0

  override def onMessage(msg: ProbearmHcdMessage): Behavior[ProbearmHcdMessage] = {
    logger.info(s"Received $msg")
    msg match {
      case Tick ⇒
        currentX = moveX
        currentY = moveY
        val currentPositionParam = IntKey.make("currentPosition").set(currentX, currentY)
        currentStatePublisher.publish(CurrentState(componentInfo.prefix, StateName("currentPosition")).add(currentPositionParam))
      case Move(x, y) ⇒
        moveX = x
        moveY = y
    }
    this
  }
}

object ProbearmHcdWorker {
  def make(currentStatePublisher: CurrentStatePublisher,
           componentInfo: ComponentInfo,
           loggerFactory: LoggerFactory): Behavior[ProbearmHcdMessage] =
    Behaviors.setup { ctx ⇒
      Behaviors.withTimers(new ProbearmHcdWorker(ctx, _, currentStatePublisher, componentInfo, loggerFactory))
    }
}
