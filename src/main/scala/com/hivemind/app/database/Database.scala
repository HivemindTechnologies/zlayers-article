package com.hivemind.app.database

import com.hivemind.app.database.Database
import com.hivemind.app.database.exception.DatabaseException
import com.hivemind.app.database.model.{DatabaseParameters, Record, TableName}
import com.hivemind.app.logging.Logger
import zio.{IO, ZIO, ZLayer}

trait Database {
  def getObjectById(id: Int, table: TableName): IO[DatabaseException, Option[Record]]
  def simulateQuery(table: TableName): IO[DatabaseException, List[Record]]
}

object Database {
  val live: ZLayer[Logger, Nothing, Database] = ZLayer.scoped {
    for {
      errorsLogger <- ZIO.service[Logger]
      databaseImpl <- ZIO.succeed(DatabaseImpl(null, null))
    } yield databaseImpl
  }
}
