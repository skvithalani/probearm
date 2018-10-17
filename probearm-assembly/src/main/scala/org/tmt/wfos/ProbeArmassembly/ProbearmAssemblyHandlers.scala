package org.tmt.wfos.ProbeArmassembly

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import csw.framework.CurrentStatePublisher
import csw.framework.scaladsl.ComponentHandlers
import csw.messages.TopLevelActorMessage
import csw.messages.commands.{CommandResponse, ControlCommand}
import csw.messages.framework.ComponentInfo
import csw.messages.location.{AkkaLocation, LocationRemoved, LocationUpdated, TrackingEvent}
import csw.messages.params.generics.KeyType.IntKey
import csw.services.alarm.api.scaladsl.AlarmService
import csw.services.command.CommandResponseManager
import csw.services.command.scaladsl.CommandService
import csw.services.event.api.scaladsl.EventService
import csw.services.location.scaladsl.LocationService
import csw.services.logging.scaladsl.LoggerFactory
import org.tmt.wfos.ProbeArmassembly.ProbearmAssemblyMessage.{DemandState, ProbearmHcd}

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
 * Domain specific logic should be written in below handlers.
 * This handlers gets invoked when component receives messages/commands from other component/entity.
 * For example, if one component sends Submit(Setup(args)) command to ProbearmHcd,
 * This will be first validated in the supervisor and then forwarded to Component TLA which first invokes validateCommand hook
 * and if validation is successful, then onSubmit hook gets invoked.
 * You can find more information on this here : https://tmtsoftware.github.io/csw-prod/framework.html
 */
class ProbearmAssemblyHandlers(
    ctx: ActorContext[TopLevelActorMessage],
    componentInfo: ComponentInfo,
    commandResponseManager: CommandResponseManager,
    currentStatePublisher: CurrentStatePublisher,
    locationService: LocationService,
    eventService: EventService,
    alarmService: AlarmService,
    loggerFactory: LoggerFactory
) extends ComponentHandlers(ctx,
                              componentInfo,
                              commandResponseManager,
                              currentStatePublisher,
                              locationService,
                              eventService,
                              alarmService,
                              loggerFactory) {

  implicit val ec: ExecutionContextExecutor = ctx.executionContext

  var probeArmHcd: Option[CommandService]       = None
  var worker: ActorRef[ProbearmAssemblyMessage] = _

  override def initialize(): Future[Unit] = {
    worker = ctx.spawnAnonymous(ProbearmAssemblyWorker.make(componentInfo, loggerFactory))
    Future.unit
  }

  override def onLocationTrackingEvent(trackingEvent: TrackingEvent): Unit =
    trackingEvent match {
      case LocationUpdated(hcdLocation: AkkaLocation) ⇒
        val commandService = new CommandService(hcdLocation)(ctx.system)
        probeArmHcd = Some(commandService)
        worker ! ProbearmHcd(commandService)
      case LocationRemoved(connection) ⇒ probeArmHcd = None
      case _                           ⇒
    }

  override def validateCommand(controlCommand: ControlCommand): CommandResponse = CommandResponse.Accepted(controlCommand.runId)

  override def onSubmit(controlCommand: ControlCommand): Unit = if (probeArmHcd.isDefined) {
    val demand = controlCommand.paramType(IntKey.make("demand"))
    worker ! DemandState(demand(0), demand(1))
  }

  override def onOneway(controlCommand: ControlCommand): Unit = {}
  override def onShutdown(): Future[Unit]                     = Future.unit
  override def onGoOffline(): Unit                            = {}
  override def onGoOnline(): Unit                             = {}

}
