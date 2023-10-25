package com.hivemind.app.logging

import zio.{Console, Ref, UIO, URLayer, ZIO, ZLayer}

trait Logger {
  def log(message: String, logLevel: HivemindLogLevel = HivemindLogLevel.INFO): UIO[Unit]
}

object Logger {
  val live: URLayer[Console, Logger] = ZLayer {
    for {
      console <- ZIO.service[Console]
      impl    <- ZIO.succeed(LoggerImpl(console))
    } yield impl
  }

  val liveWithLineCounter: URLayer[Console, Logger] = ZLayer.scoped {
    for {
      ref     <- Ref.make(0)
      console <- ZIO.service[Console]
      impl    <- ZIO.succeed(LoggerWithLineCounter(console, ref))
      _       <- impl.initializer
      _       <- ZIO.addFinalizer(impl.finalizer)
    } yield impl
  }
}
