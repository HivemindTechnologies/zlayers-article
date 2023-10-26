package com.hivemind.app.config

import com.hivemind.app.database.exception.{DatabaseException, DatabaseLayerExecutionOutcome}

case class DatabaseParameters(
  databaseName: String,
  databasePassword: String,
  maxConnections: Int,
  outcome: DatabaseLayerExecutionOutcome,
)

object DatabaseParameters {
  def testParameters(outcome: DatabaseLayerExecutionOutcome): DatabaseParameters =
    DatabaseParameters("myDB", "password", 5, outcome = outcome)
}
