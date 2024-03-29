lazy val root        = project
  .in(file("."))
  .settings(
    name         := "zlayers-article",
    organization := "com.hivemind",
    description  := "Sample project using Scala 3 and ZIO Layers",
    version      := "1.0.0",
    scalaVersion := "3.3.1",
  )
val ZIOVersion       = "2.0.18"
val ScalaTestVersion = "3.2.17"

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "clean; fmt; all compile Test/compile IntegrationTest/compile; test")

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

libraryDependencies ++= Seq(
  // ZIO
  "dev.zio"       %% "zio"          % ZIOVersion,
  "dev.zio"       %% "zio-streams"  % ZIOVersion,
  "dev.zio"       %% "zio-test"     % ZIOVersion       % "test",
  "dev.zio"       %% "zio-test-sbt" % ZIOVersion       % "test",
  "org.scalactic" %% "scalactic"    % ScalaTestVersion,
  "org.scalatest" %% "scalatest"    % ScalaTestVersion % "test",
)

resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"

scalacOptions ++= Seq(
  "-deprecation",     // emit warning and location for usages of deprecated APIs
  "-explain",         // explain errors in more detail
  "-explain-types",   // explain type errors in more detail
  "-feature",         // emit warning and location for usages of features that should be imported explicitly
  "-indent",          // allow significant indentation.
  "-new-syntax",      // require `then` and `do` in control expressions.
  "-print-lines",     // show source code line numbers.
  "-unchecked",       // enable additional warnings where generated code depends on assumptions
  "-Ykind-projector", // allow `*` as wildcard to be compatible with kind projector
  "-Xfatal-warnings", // fail the compilation if there are any warnings
  "-Xmigration",      // warn about constructs whose behavior may have changed since version
)
