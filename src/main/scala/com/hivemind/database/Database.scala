package com.hivemind.database

import com.hivemind.database.exception.DatabaseException
import com.hivemind.database.model.{Record, TableName}
import zio.IO

trait Database {
  def getObjectById(id: Int, table: TableName): IO[DatabaseException, Option[Record]]
  def simulateQuery(table: TableName): IO[DatabaseException, List[Record]]
}
