package org.tmt.wfos.ProbeArmassembly

import akka.actor.typed.scaladsl.{Behaviors, MutableBehavior}
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.Behavior
import akka.util.Timeout
import csw.messages.commands.{CommandName, Setup}
import csw.messages.framework.ComponentInfo
import csw.messages.params.generics.KeyType.IntKey
import csw.messages.params.states.{CurrentState, StateName}
import csw.services.command.scaladsl.CommandService
import csw.services.logging.scaladsl.{Logger, LoggerFactory}
import org.tmt.wfos.ProbeArmassembly.ProbearmAssemblyMessage.{
  CurrentStateContainer,
  DemandState,
  ProbearmHcd,
  ProbearmHcdRemoved$
}

import scala.concurrent.duration.DurationDouble

class ProbearmAssemblyWorker(ctx: ActorContext[ProbearmAssemblyMessage],
                             componentInfo: ComponentInfo,
                             loggerFactory: LoggerFactory)
    extends MutableBehavior[ProbearmAssemblyMessage] {

  private val logger: Logger = loggerFactory.getLogger(ctx)

  var demandState: Option[DemandState]    = None
  var currentState: Option[CurrentState]  = None
  var probeArmHcd: Option[CommandService] = None

  override def onMessage(msg: ProbearmAssemblyMessage): Behavior[ProbearmAssemblyMessage] = {
    logger.info(s"Received $msg")
    msg match {
      case demand: DemandState ⇒ demandState = Some(demand)
      case CurrentStateContainer(currentValue) ⇒
        currentState = Some(currentValue)
        for {
          demand      ← demandState
          probeArmHcd ← probeArmHcd
          currentParam = currentValue(IntKey.make("currentPosition"))
          x: Int       = demand.x - currentParam(0)
          y: Int       = demand.y - currentParam(1)
          param        = IntKey.make("move").set(x, y)
          setup        = Setup(componentInfo.prefix, CommandName("move"), None).add(param)
          _            = probeArmHcd.oneway(setup)(Timeout(5.seconds))
        } yield ()

      case ProbearmHcd(hcd) ⇒
        probeArmHcd = Some(hcd)
        hcd.subscribeOnlyCurrentState(Set(StateName("currentPosition")), ctx.self ! CurrentStateContainer(_))
      case ProbearmHcdRemoved$ ⇒ probeArmHcd = None
    }
    this
  }
}

object ProbearmAssemblyWorker {
  def make(componentInfo: ComponentInfo, loggerFactory: LoggerFactory): Behavior[ProbearmAssemblyMessage] =
    Behaviors.setup(new ProbearmAssemblyWorker(_, componentInfo, loggerFactory))
}
