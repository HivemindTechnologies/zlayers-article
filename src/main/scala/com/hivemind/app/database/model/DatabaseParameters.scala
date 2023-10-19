package com.hivemind.app.database.model

case class DatabaseParameters(databaseName: String, databasePassword: String, maxConnections: Int = 10, maxSecondsForQuery: Int = 5)
