package org.tmt.wfos.ProbeArmdeploy

import csw.framework.deploy.hostconfig.HostConfig

object ProbearmHostConfigApp extends App {

  HostConfig.start("probearm-host-config-app", args)

}
