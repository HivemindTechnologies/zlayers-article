package com.hivemind.app.config

import com.hivemind.app.database.exception.{DatabaseException, DatabaseLayerExecutionOutcome}
import zio.{ULayer, ZLayer}

case class Config(databaseParameters: DatabaseParameters)

object Config {
  private lazy val liveDBParams: DatabaseParameters =
    DatabaseParameters(
      databaseName = "myMemoryDB",
      databasePassword = "password",
      maxConnections = 5,
      outcome = DatabaseLayerExecutionOutcome.FinishWithoutErrors,
    )

  val live: ULayer[Config] =
    ZLayer.succeed(
      Config(databaseParameters = liveDBParams),
    )

  def testConfig(outcome: DatabaseLayerExecutionOutcome): Config =
    Config(DatabaseParameters.testParameters(outcome))
}
