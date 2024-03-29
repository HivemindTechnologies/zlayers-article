package com.hivemind.app.database

import com.hivemind.app.config.Config
import com.hivemind.app.database.exception.DatabaseException
import com.hivemind.app.database.model.{Record, TableName}
import com.hivemind.app.logging.Logger
import zio.{IO, URLayer, ZIO, ZLayer}

trait Database {
  def getObjectById(id: Int, table: TableName): IO[DatabaseException, Option[Record]]
  def getAllRecords(table: TableName): IO[DatabaseException, List[Record]]
}

object Database {
  val live: URLayer[Logger with Config, Database] = ZLayer {
    for {
      logger <- ZIO.service[Logger]
      config <- ZIO.service[Config]
    } yield DatabaseImpl(config.databaseParameters, logger)
  }
}
