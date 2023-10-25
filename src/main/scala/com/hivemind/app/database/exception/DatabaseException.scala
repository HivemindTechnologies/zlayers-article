package com.hivemind.app.database.exception

import com.hivemind.app.logging.{HivemindLogLevel, Logger}
import com.hivemind.app.model.ApplicationError
import zio.UIO

sealed trait DatabaseException extends ApplicationError

case class DatabaseTimeoutException() extends DatabaseException {

  override def logError(logger: Logger): UIO[String] = {
    val message = "A timeout occurred in the database layer."
    for {
      _ <- logger.log(message, HivemindLogLevel.ERROR)
    } yield message
  }
}
case class DatabaseQueryExecutionException()   extends DatabaseException {
  override def logError(logger: Logger): UIO[String] = {
    val message = "An exception occurred while querying the database."
    for {
      _ <- logger.log(message, HivemindLogLevel.ERROR)
    } yield message
  }
}
case class DatabaseConnectionClosedException() extends DatabaseException {
  override def logError(logger: Logger): UIO[String] = {
    val message = "The connection closed unexpectedly while querying the database."
    for {
      _ <- logger.log(message, HivemindLogLevel.ERROR)
    } yield message
  }
}
