package org.tmt.wfos.Tcsassembly

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, MutableBehavior, TimerScheduler}
import akka.util.Timeout
import csw.messages.commands.{CommandName, Setup}
import csw.messages.framework.ComponentInfo
import csw.messages.params.generics.KeyType.IntKey
import csw.services.command.scaladsl.CommandService
import csw.services.logging.scaladsl.LoggerFactory
import org.tmt.wfos.Tcsassembly.TcsAssemblyMessage.{ProbeArmAssembly, ProbeArmAssemblyRemoved, Tick}

import scala.concurrent.duration.DurationDouble

class TcsAssemblyWorker(ctx: ActorContext[TcsAssemblyMessage],
                        componentInfo: ComponentInfo,
                        timer: TimerScheduler[TcsAssemblyMessage],
                        loggerFactory: LoggerFactory)
    extends MutableBehavior[TcsAssemblyMessage] {

  implicit val timeout: Timeout = Timeout(5.seconds)
  private val logger            = loggerFactory.getLogger(ctx)

  var x                                        = 0
  var y                                        = 0
  var probeArmAssembly: Option[CommandService] = None

  override def onMessage(msg: TcsAssemblyMessage): Behavior[TcsAssemblyMessage] = {
    msg match {
      case Tick ⇒
        x = x + 10
        y = y + 25
        val demandParam = IntKey.make("demand").set(x, y)
        val setup       = Setup(componentInfo.prefix, CommandName("DemandState"), None).add(demandParam)
        probeArmAssembly.foreach(_.submit(setup))
      case ProbeArmAssembly(assembly) ⇒
        logger.info(s"Received $msg")
        probeArmAssembly = Some(assembly)
        timer.startPeriodicTimer("key", Tick, 1000.millis)
      case ProbeArmAssemblyRemoved ⇒
        logger.info(s"Received $msg")
        timer.cancel("key")
    }
    this
  }
}

object TcsAssemblyWorker {
  def make(componentInfo: ComponentInfo, loggerFactory: LoggerFactory): Behavior[TcsAssemblyMessage] =
    Behaviors.setup(ctx ⇒ {
      Behaviors.withTimers(new TcsAssemblyWorker(ctx, componentInfo, _, loggerFactory))
    })
}
