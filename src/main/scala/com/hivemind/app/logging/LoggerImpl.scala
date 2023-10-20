package com.hivemind.app.logging

import zio.{Console, UIO}

case class LoggerImpl(console: Console) extends Logger {
  override def log(message: String, logLevel: HivemindLogLevel = HivemindLogLevel.INFO): UIO[Unit] =
    console.printLine(s"${logLevel.getPrefix}: " + message).ignore
}
