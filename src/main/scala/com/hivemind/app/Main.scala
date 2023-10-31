package com.hivemind.app

import com.hivemind.app.config.Config
import com.hivemind.app.database.Database
import com.hivemind.app.logging.{HivemindLogLevel, Logger}
import com.hivemind.app.repository.property.PropertyRepository
import com.hivemind.app.repository.user.UserRepository
import com.hivemind.app.service.property.PropertyService
import com.hivemind.app.service.user.UserService
import zio.{ZIO, ZIOAppDefault, ZLayer}

import java.io.IOException

object Main extends ZIOAppDefault {

  private val myAppLogic: ZIO[Logger with UserService with PropertyService, IOException, Unit] =
    for {
      logging     <- ZIO.service[Logger]
      userService <- ZIO.service[UserService]
      propertyService <- ZIO.service[PropertyService]
      _      <- logging.log("Starting application ...", HivemindLogLevel.INFO)
      _      <- logging.log("Looking for user with id=1")
      alonzo <- userService
                  .findUser(1)
                  .mapError(err => IOException(s"UserService failed to find user with id 1", err))
                  .flatMap(ZIO.fromOption)
                  .orElseFail(IOException(s"Did not find user with Iid 1"))
      _      <- logging.log(s"Found user '${alonzo.name} ${alonzo.surname}'")
      _      <- logging.log(s"Looking for ${alonzo.name}'s properties")
      properties <- propertyService
        .findPropertiesOfUser(alonzo.id)
        .mapError(err => IOException(s"PropertyService failed to find properties for user with id ${alonzo.id}", err))
      propertiesAsStr = properties.map(property => s"{ id: ${property.id}, kind: ${property.kind}, price: €${property.price} }").mkString("\n")
      _ <- logging.log(s"User ${alonzo.name} has the following properties:\n$propertiesAsStr")
      _ <- logging.log("Ending application")
    } yield ()

  def run: ZIO[Any, IOException, Unit] =
    myAppLogic.provide(
      Logger.live,
      ZLayer.succeed(zio.Console.ConsoleLive),
      PropertyService.live,
      PropertyRepository.live,
      UserService.live,
      UserRepository.live,
      Database.live,
      Config.live,
//      ZLayer.Debug.mermaid // (uncomment to show a dependency graph in a diagram)
//      ZLayer.Debug.tree // (uncomment to show a dependency graph in console)
    )
}
