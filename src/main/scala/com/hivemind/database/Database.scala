package com.hivemind.database

import com.hivemind.database.exception.DatabaseException
import com.hivemind.database.model.{Record, TableName}
import zio.IO

trait Database {
  def getObjectById(id: Int, table: TableName): IO[DatabaseException, Option[Record]]
  def executeQuery(query: String): IO[DatabaseException, List[Record]]
}
