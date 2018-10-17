lazy val `tcs-assembly` = project
  .settings(
    libraryDependencies ++= Dependencies.TcsAssembly
  )

lazy val `probearm-assembly` = project
  .settings(
    libraryDependencies ++= Dependencies.ProbearmAssembly
  )

lazy val `probearm-hcd` = project
  .settings(
    libraryDependencies ++= Dependencies.ProbearmHcd
  )

lazy val `probearm-deploy` = project
  .dependsOn(
    `tcs-assembly`,
    `probearm-assembly`,
    `probearm-hcd`
  )
  .enablePlugins(JavaAppPackaging, CswBuildInfo)
  .settings(
    libraryDependencies ++= Dependencies.ProbearmDeploy
  )
