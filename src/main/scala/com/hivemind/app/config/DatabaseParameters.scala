package com.hivemind.app.config

case class DatabaseParameters(databaseName: String, databasePassword: String, maxConnections: Int, probabilityOfError: Double)
