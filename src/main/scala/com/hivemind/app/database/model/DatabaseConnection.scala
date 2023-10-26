package com.hivemind.app.database.model

import com.hivemind.app.database.exception.DatabaseLayerExecutionOutcome

case class DatabaseConnection(isAlive: Boolean)

object DatabaseConnection {
  def fromDatabaseLayerOutcome(outcome: DatabaseLayerExecutionOutcome): DatabaseConnection =
    DatabaseConnection(isAlive = outcome != DatabaseLayerExecutionOutcome.RaiseConnectionClosedError)
}
