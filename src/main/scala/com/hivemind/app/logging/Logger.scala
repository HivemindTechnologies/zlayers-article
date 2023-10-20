package com.hivemind.app.logging

import zio.{Console, UIO, ZIO, ZLayer}

trait Logger {
  def log(message: String, logLevel: HivemindLogLevel = HivemindLogLevel.INFO): UIO[Unit]
}

object Logger {
  val live: ZLayer[Console, Nothing, Logger] = ZLayer.scoped {
    for {
      console <- ZIO.succeed(Console.ConsoleLive)
      impl    <- ZIO.succeed(LoggerImpl(console))
    } yield impl
  }
}
