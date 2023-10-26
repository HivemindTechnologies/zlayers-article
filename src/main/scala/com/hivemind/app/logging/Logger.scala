package com.hivemind.app.logging

import zio.{Console, Ref, Scope, UIO, URLayer, ZIO, ZLayer}

trait Logger {
  def log(message: String, logLevel: HivemindLogLevel = HivemindLogLevel.INFO): UIO[Unit]
}

object Logger {
  val live: URLayer[Console, Logger] = ZLayer {
    for {
      console <- ZIO.service[Console]
    } yield LoggerImpl(console)
  }

  def liveWithLineCounter: URLayer[Scope with Console, Logger] =
    ZLayer {
      for {
        ref     <- Ref.make(0)
        console <- ZIO.service[Console]
        impl    <- ZIO.succeed(LoggerWithLineCounter(console, ref))
        _       <- impl.initializer
        _       <- ZIO.addFinalizer(impl.finalizer)
      } yield impl
    }

  def liveFromFunction: URLayer[Console, Logger] =
    ZLayer.fromFunction((console: Console) => LoggerImpl(console))

}
