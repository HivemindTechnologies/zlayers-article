package com.hivemind.logging

import zio.{Console, UIO, ZIO, ZLayer}

trait Logging {
  def log(message: String, logLevel: MyLogLevel = MyLogLevel.INFO): UIO[Unit]
}

object Logging {

  private case class LoggingImpl(console: Console) extends Logging {
    override def log(message: String,
                     logLevel: MyLogLevel = MyLogLevel.INFO): UIO[Unit] =
      console.printLine(s"${logLevel.getPrefix}: " + message).ignore
  }

  val live: ZLayer[Console, Nothing, Logging] = ZLayer.scoped {
    for {
      console <- ZIO.service[Console]
      impl <- ZIO.succeed(LoggingImpl(console))
    } yield impl
  }
}
