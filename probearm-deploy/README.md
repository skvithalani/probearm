# ProbeArm-deploy

This module contains apps and configuration files for host deployment using 
HostConfig (https://tmtsoftware.github.io/csw-prod/apps/hostconfig.html) and 
ContainerCmd (https://tmtsoftware.github.io/csw-prod/framework/deploying-components.html).

An important part of making this work is ensuring the host config app (ProbearmHostConfigApp) is built
with all of the necessary dependencies of the components it may run.  This is done by adding settings to the
built.sbt file:

```
lazy val `ProbeArm-deploy` = project
  .dependsOn(
    `ProbeArm-assembly`,
    `ProbeArm-hcd`
  )
  .enablePlugins(JavaAppPackaging)
  .settings(
    libraryDependencies ++= Dependencies.ProbearmDeploy
  )
```

and in Libs.scala:

```

  val `csw-framework`  = "org.tmt" %% "csw-framework"  % Version

```

To start ProbeArm Assembly and HCD, follow below steps:

 - Run `sbt ProbeArm-deploy/universal:packageBin`, this will create self contained zip in target/universal directory
 - Unzip generate zip and enter into bin directory
 - Run container cmd script or host config app script
 - Ex.  `./ProbeArm-host-config-app --local ../../../../ProbeArm-deploy/src/main/resources/ProbearmHostConfig.conf -s ./ProbeArm-container-cmd-app`

Note: the CSW Location Service cluster seed must be running, and appropriate environment variables set to run apps.
See https://tmtsoftware.github.io/csw-prod/apps/cswclusterseed.html .