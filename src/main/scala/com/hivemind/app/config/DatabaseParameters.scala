package com.hivemind.app.config

case class DatabaseParameters(databaseName: String, databasePassword: String, maxConnections: Int, queryTimeoutSeconds: Int, probabilityOfError: Double = 0.0)
