package org.tmt.wfos.ProbeArmassembly

import csw.messages.TMTSerializable
import csw.messages.params.states.CurrentState
import csw.services.command.scaladsl.CommandService

sealed trait ProbearmAssemblyMessage extends TMTSerializable

object ProbearmAssemblyMessage {
  case class DemandState(x: Int, y: Int)                       extends ProbearmAssemblyMessage
  case class CurrentStateContainer(currentState: CurrentState) extends ProbearmAssemblyMessage
  case class ProbearmHcd(hcd: CommandService)                  extends ProbearmAssemblyMessage
  case object ProbearmHcdRemoved$                              extends ProbearmAssemblyMessage
}
