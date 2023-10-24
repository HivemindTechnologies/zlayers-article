package com.hivemind.app.logging

import zio.{Console, UIO, URLayer, ZIO, ZLayer}

trait Logger {
  def log(message: String, logLevel: HivemindLogLevel = HivemindLogLevel.INFO): UIO[Unit]
}

object Logger {
  val live: URLayer[Console, Logger] = ZLayer {
    for {
      console <- ZIO.succeed(Console.ConsoleLive)
      impl    <- ZIO.succeed(LoggerImpl(console))
    } yield impl
  }
}
