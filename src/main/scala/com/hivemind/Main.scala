package com.hivemind

import com.hivemind.logging.{Logging, MyLogLevel}
import zio.*

import java.io.IOException

object Main extends ZIOAppDefault {

  private val mainApp: ZIO[Logging, IOException, Unit] =
    for {
      logging <- ZIO.service[Logging]
      _ <- logging.log("Starting application ...", MyLogLevel.INFO)
    } yield ()

  def run: ZIO[Any, IOException, Unit] =
    mainApp.provide(
      Logging.live,
      ZLayer.succeed(Console.ConsoleLive),
//      ZLayer.Debug.mermaid // (uncomment to show a dependency graph in a diagram)
//      ZLayer.Debug.tree // (uncomment to show a dependency graph in console)
    )
}
