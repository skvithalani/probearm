package org.tmt.wfos.ProbeArmdeploy

import csw.framework.deploy.containercmd.ContainerCmd

object ProbearmContainerCmdApp extends App {

  ContainerCmd.start("probearm-container-cmd-app", args)

}
