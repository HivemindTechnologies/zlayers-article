package com.hivemind.repository.exception

import com.hivemind.model.ApplicationError
import zio.{Console, UIO}

sealed trait RepositoryException(message: String) extends ApplicationError {
  override def logError: UIO[String] =
    this match {
      case RepositoryConnectionError(error) =>
        val message = s"A connection exception occurred in the repository: $error"
        for {
          _ <- Console.printLine(message).ignore
        } yield message
    }
}

case class RepositoryConnectionError(message: String) extends RepositoryException(message)
