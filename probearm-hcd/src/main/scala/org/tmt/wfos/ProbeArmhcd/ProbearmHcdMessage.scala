package org.tmt.wfos.ProbeArmhcd

sealed trait ProbearmHcdMessage

object ProbearmHcdMessage {
  case class Move(x: Int, y: Int) extends ProbearmHcdMessage
  case object Tick                extends ProbearmHcdMessage
}
