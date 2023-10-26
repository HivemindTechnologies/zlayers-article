package com.hivemind.app.service.exception

import com.hivemind.app.logging.{HivemindLogLevel, Logger}
import com.hivemind.app.model.ApplicationError
import com.hivemind.app.repository.exception.{RepositoryConnectionError, RepositoryException}
import zio.{IO, UIO, ZIO}

sealed trait ServiceException extends ApplicationError

case class ServiceConnectionError() extends ServiceException {
  override def logError(logger: Logger): UIO[String] =
    val message = "A connection exception occurred in the service layer."
    for {
      _ <- logger.log(message, HivemindLogLevel.ERROR)
    } yield message
}

object ServiceException {
  def handleRepositoryErrors[A](zio: IO[RepositoryException, A], logger: Logger): IO[ServiceException, A] =
    zio.catchAll { case _: RepositoryConnectionError =>
      val serviceError = ServiceConnectionError()
      serviceError.logError(logger) *>
        ZIO.fail[ServiceException](ServiceConnectionError())
    }
}
