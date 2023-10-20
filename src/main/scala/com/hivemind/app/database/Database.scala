package com.hivemind.app.database

import com.hivemind.app.config.Config
import com.hivemind.app.database.exception.DatabaseException
import com.hivemind.app.database.model.{Record, TableName}
import com.hivemind.app.logging.Logger
import zio.{IO, ZIO, ZLayer}

trait Database {
  def getObjectById(id: Int, table: TableName): IO[DatabaseException, Option[Record]]
  def simulateQuery(table: TableName): IO[DatabaseException, List[Record]]
}

object Database {
  val live: ZLayer[Logger with Config, Nothing, Database] = ZLayer.scoped {
    for {
      errorsLogger <- ZIO.service[Logger]
      config       <- ZIO.service[Config]
      databaseImpl <- ZIO.succeed(DatabaseImpl(config.databaseParameters, errorsLogger))
    } yield databaseImpl
  }
}
