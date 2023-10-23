package com.hivemind.app.repository.exception

import com.hivemind.app.model.ApplicationError
import zio.{Console, UIO}

sealed trait RepositoryException extends ApplicationError {
  override def logError(): UIO[String] =
    this match {
      case RepositoryConnectionError =>
        val message = s"A connection exception occurred in the repository."
        for {
          _ <- Console.printLine(message).ignore
        } yield message
    }
}

case object RepositoryConnectionError extends RepositoryException
