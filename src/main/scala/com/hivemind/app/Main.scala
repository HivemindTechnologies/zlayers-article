package com.hivemind.app

import com.hivemind.logging.{Logger, HivemindLogLevel}
import zio.*

import java.io.IOException

object Main extends ZIOAppDefault {

  private val myAppLogic: ZIO[Logger, IOException, Unit] =
    for {
      logging <- ZIO.service[Logger]
      _       <- logging.log("Starting application ...", HivemindLogLevel.INFO)
    } yield ()

  def run: ZIO[Any, IOException, Unit] =
    myAppLogic.provide(
      Logger.live,
      ZLayer.succeed(Console.ConsoleLive),
//      ZLayer.Debug.mermaid // (uncomment to show a dependency graph in a diagram)
//      ZLayer.Debug.tree // (uncomment to show a dependency graph in console)
    )
}
