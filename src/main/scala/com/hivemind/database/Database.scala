package com.hivemind.database

import com.hivemind.database.exception.DatabaseException
import com.hivemind.database.model.{Record, TableName}
import zio.IO

import java.util.UUID

trait Database {
  def getObjectById(id: UUID, table: TableName): IO[DatabaseException, Option[Record]]
  def executeQuery(query: String): IO[DatabaseException, List[Record]]
}
