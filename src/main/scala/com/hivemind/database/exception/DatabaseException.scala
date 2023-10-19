package com.hivemind.database.exception

import com.hivemind.model.ApplicationError
import zio.{Console, UIO}
sealed trait DatabaseException extends ApplicationError {
  override def logError: UIO[String] =
    this match {
      case DatabaseTimeoutException =>
        val message = "A timeout occurred in the database"
        for {
          _ <- Console.printLine(message).ignore
        } yield message
      case DatabaseConnectionError  =>
        val message = "A connection exception occurred in the database"
        for {
          _ <- Console.printLine(message).ignore
        } yield message
    }
}

case object DatabaseTimeoutException extends DatabaseException
case object DatabaseConnectionError  extends DatabaseException
