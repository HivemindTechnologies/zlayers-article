package com.hivemind.app.repository.exception

import com.hivemind.app.database.exception.{DatabaseConnectionClosedException, DatabaseException, DatabaseQueryExecutionException, DatabaseTimeoutException}
import com.hivemind.app.logging.{HivemindLogLevel, Logger}
import com.hivemind.app.model.ApplicationError
import zio.{IO, UIO, ZIO}

sealed trait RepositoryException extends ApplicationError

case class RepositoryConnectionError() extends RepositoryException {
  override def logError(logger: Logger): UIO[String] =
    val message = s"A connection exception occurred in the repository."
    for {
      _ <- logger.log(message, HivemindLogLevel.ERROR)
    } yield message
}

object RepositoryException {

  def handleDatabaseErrors[A](zio: IO[DatabaseException, A], logger: Logger): IO[RepositoryException, A] =
    zio.mapError {
      case DatabaseTimeoutException()          =>
        RepositoryConnectionError()
      case DatabaseQueryExecutionException()   =>
        RepositoryConnectionError()
      case DatabaseConnectionClosedException() =>
        RepositoryConnectionError()
    }.catchSome { case error: RepositoryException =>
      error.logError(logger) *>
        ZIO.fail[RepositoryException](error)
    }
}
