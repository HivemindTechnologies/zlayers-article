package com.hivemind.service.exception

import com.hivemind.model.ApplicationError
import zio.{Console, UIO}

sealed trait ServiceException extends ApplicationError {
  override def logError(): UIO[String] =
    this match {
      case ServiceConnectionError =>
        val message = s"A connection exception occurred in the service layer."
        for {
          _ <- Console.printLine(message).ignore
        } yield message
    }
}

case object ServiceConnectionError extends ServiceException
