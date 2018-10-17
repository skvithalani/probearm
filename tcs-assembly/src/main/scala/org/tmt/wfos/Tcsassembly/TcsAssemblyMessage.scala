package org.tmt.wfos.Tcsassembly

import csw.services.command.scaladsl.CommandService

sealed trait TcsAssemblyMessage

object TcsAssemblyMessage {
  case class ProbeArmAssembly(assembly: CommandService) extends TcsAssemblyMessage
  case object ProbeArmAssemblyRemoved                   extends TcsAssemblyMessage
  case object Tick                                      extends TcsAssemblyMessage
}
