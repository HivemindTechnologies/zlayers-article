package com.hivemind.app.config

case class DatabaseParameters(databaseName: String, databasePassword: String, maxConnections: Int, probabilityOfError: Double)

object DatabaseParameters {
  def testParameters(probabilityOfErrors: Double): DatabaseParameters =
    DatabaseParameters("myDB", "password", 5, probabilityOfError = probabilityOfErrors)
}
