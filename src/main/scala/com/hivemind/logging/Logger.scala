package com.hivemind.logging

import zio.{Console, UIO, ZIO, ZLayer}

trait Logger {
  def log(message: String, logLevel: HivemindLogLevel = HivemindLogLevel.INFO): UIO[Unit]
}

object Logger {
  private case class LoggerImpl(console: Console) extends Logger {
    override def log(message: String, logLevel: HivemindLogLevel = HivemindLogLevel.INFO): UIO[Unit] =
      console.printLine(s"${logLevel.getPrefix}: " + message).ignore
  }

  val live: ZLayer[Console, Nothing, Logger] = ZLayer.scoped {
    for {
      console <- ZIO.service[Console]
      impl    <- ZIO.succeed(LoggerImpl(console))
    } yield impl
  }
}
