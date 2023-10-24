package com.hivemind.app.logging

import zio.{Console, Ref, UIO}
case class LoggerWithLineCounter(console: Console, counter: Ref[Int]) extends Logger {
  def initializer: UIO[Unit] = console.printLine("Initializing logger instance ...").ignore

  override def log(message: String, logLevel: HivemindLogLevel = HivemindLogLevel.INFO): UIO[Unit] =
    for {
      _ <- counter.update(_ + 1)
      _ <- console.printLine(s"${logLevel.getPrefix}: " + message).ignore
    } yield ()

  def finalizer: UIO[Unit] =
    for {
      lines <- counter.get
      _     <- console.printLine(s"Total printed lines: $lines").ignore
    } yield ()
}
