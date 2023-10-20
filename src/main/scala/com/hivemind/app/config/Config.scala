package com.hivemind.app.config

import zio.ZLayer

case class Config(databaseParameters: DatabaseParameters)

object Config {
  val live: ZLayer[Any, Nothing, Config]     =
    ZLayer.succeed(
      Config(databaseParameters = myDBParams),
    )
  private val myDBParams: DatabaseParameters =
    DatabaseParameters(databaseName = "myMemoryDB", databasePassword = "password", maxConnections = 5, probabilityOfError = 10.0)
}
