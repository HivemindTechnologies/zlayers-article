package com.hivemind.app.config

import zio.{ULayer, ZLayer}

case class Config(databaseParameters: DatabaseParameters)

object Config {
  val live: ULayer[Config]                   =
    ZLayer.succeed(
      Config(databaseParameters = myDBParams),
    )
  private val myDBParams: DatabaseParameters =
    DatabaseParameters(databaseName = "myMemoryDB", databasePassword = "password", maxConnections = 5, probabilityOfError = 10.0)

  def testConfig(probabilityOfErrors: Double): Config =
    Config(DatabaseParameters.testParameters(probabilityOfErrors))
}
