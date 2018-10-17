package org.tmt.wfos.Tcsassembly

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import csw.framework.CurrentStatePublisher
import csw.framework.scaladsl.ComponentHandlers
import csw.messages.TopLevelActorMessage
import csw.messages.commands.CommandResponse.Accepted
import csw.messages.commands.{CommandResponse, ControlCommand}
import csw.messages.framework.ComponentInfo
import csw.messages.location.{AkkaLocation, LocationRemoved, LocationUpdated, TrackingEvent}
import csw.services.alarm.api.scaladsl.AlarmService
import csw.services.command.CommandResponseManager
import csw.services.command.scaladsl.CommandService
import csw.services.event.api.scaladsl.EventService
import csw.services.location.scaladsl.LocationService
import csw.services.logging.scaladsl.LoggerFactory
import org.tmt.wfos.Tcsassembly.TcsAssemblyMessage.{ProbeArmAssembly, ProbeArmAssemblyRemoved}

import scala.concurrent.Future

class TcsAssemblyHandlers(ctx: ActorContext[TopLevelActorMessage],
                          componentInfo: ComponentInfo,
                          commandResponseManager: CommandResponseManager,
                          currentStatePublisher: CurrentStatePublisher,
                          locationService: LocationService,
                          eventService: EventService,
                          alarmService: AlarmService,
                          loggerFactory: LoggerFactory)
    extends ComponentHandlers(
      ctx,
      componentInfo,
      commandResponseManager,
      currentStatePublisher,
      locationService,
      eventService,
      alarmService,
      loggerFactory
    ) {

  var worker: ActorRef[TcsAssemblyMessage] = _
  override def initialize(): Future[Unit] = {
    worker = ctx.spawnAnonymous(TcsAssemblyWorker.make(componentInfo, loggerFactory))
    Future.unit
  }

  override def onLocationTrackingEvent(trackingEvent: TrackingEvent): Unit =
    trackingEvent match {
      case LocationUpdated(loc: AkkaLocation) ⇒ worker ! ProbeArmAssembly(new CommandService(loc)(ctx.system))
      case LocationRemoved(con)               ⇒ worker ! ProbeArmAssemblyRemoved
      case _                                  ⇒
    }

  override def validateCommand(controlCommand: ControlCommand): CommandResponse = Accepted(controlCommand.runId)

  override def onSubmit(controlCommand: ControlCommand): Unit = {}
  override def onOneway(controlCommand: ControlCommand): Unit = {}
  override def onShutdown(): Future[Unit]                     = Future.unit
  override def onGoOffline(): Unit                            = {}
  override def onGoOnline(): Unit                             = {}
}
