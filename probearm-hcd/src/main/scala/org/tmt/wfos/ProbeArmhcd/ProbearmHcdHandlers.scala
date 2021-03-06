package org.tmt.wfos.ProbeArmhcd

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import csw.framework.CurrentStatePublisher
import csw.framework.scaladsl.ComponentHandlers
import csw.messages.TopLevelActorMessage
import csw.messages.commands.{CommandResponse, ControlCommand}
import csw.messages.framework.ComponentInfo
import csw.messages.location.TrackingEvent
import csw.messages.params.generics.KeyType.IntKey
import csw.services.command.CommandResponseManager
import csw.services.event.api.scaladsl.EventService
import csw.services.alarm.api.scaladsl.AlarmService
import csw.services.location.scaladsl.LocationService
import csw.services.logging.scaladsl.LoggerFactory
import org.tmt.wfos.ProbeArmhcd.ProbearmHcdMessage.Move

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
 * Domain specific logic should be written in below handlers.
 * This handlers gets invoked when component receives messages/commands from other component/entity.
 * For example, if one component sends Submit(Setup(args)) command to ProbearmHcd,
 * This will be first validated in the supervisor and then forwarded to Component TLA which first invokes validateCommand hook
 * and if validation is successful, then onSubmit hook gets invoked.
 * You can find more information on this here : https://tmtsoftware.github.io/csw-prod/framework.html
 */
class ProbearmHcdHandlers(
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

  var worker: ActorRef[ProbearmHcdMessage] = _

  override def initialize(): Future[Unit] = {
    worker = ctx.spawnAnonymous(ProbearmHcdWorker.make(currentStatePublisher, componentInfo, loggerFactory))
    Future.unit
  }

  override def onLocationTrackingEvent(trackingEvent: TrackingEvent): Unit = {}

  override def validateCommand(controlCommand: ControlCommand): CommandResponse = CommandResponse.Accepted(controlCommand.runId)

  override def onSubmit(controlCommand: ControlCommand): Unit = {
    val moveParam = controlCommand.paramType(IntKey.make("move"))
    worker ! Move(moveParam(0), moveParam(1))
  }

  override def onOneway(controlCommand: ControlCommand): Unit = {
    val moveParam = controlCommand.paramType(IntKey.make("move"))
    worker ! Move(moveParam(0), moveParam(1))
  }

  override def onShutdown(): Future[Unit] = Future.unit
  override def onGoOffline(): Unit        = {}
  override def onGoOnline(): Unit         = {}

}
