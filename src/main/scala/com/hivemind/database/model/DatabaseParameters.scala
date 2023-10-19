package com.hivemind.database.model

case class DatabaseParameters(databaseName: String, databasePassword: String, maxConnections: Int = 10, maxSecondsForQuery: Int = 5)
