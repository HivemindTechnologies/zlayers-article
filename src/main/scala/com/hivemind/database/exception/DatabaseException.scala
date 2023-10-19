package com.hivemind.database.exception

import com.hivemind.logging.{HivemindLogLevel, Logger}
import com.hivemind.model.ApplicationError
import zio.UIO

sealed trait DatabaseException                      extends ApplicationError
case class DatabaseTimeoutException(logger: Logger) extends DatabaseException {

  override def logError(): UIO[String] = {
    val message = "A timeout occurred in the database"
    for {
      _ <- logger.log(message, HivemindLogLevel.ERROR)
    } yield message
  }
}
case class DatabaseQueryExecutionException(logger: Logger)   extends DatabaseException {
  override def logError(): UIO[String] = {
    val message = "A connection exception occurred while querying the database."
    for {
      _ <- logger.log(message, HivemindLogLevel.ERROR)
    } yield message
  }
}
case class DatabaseConnectionClosedException(logger: Logger) extends DatabaseException {
  override def logError(): UIO[String] = {
    val message = "The connection closed unexpectedly while querying the database."
    for {
      _ <- logger.log(message, HivemindLogLevel.ERROR)
    } yield message
  }
}
