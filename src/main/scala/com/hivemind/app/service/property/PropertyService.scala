package com.hivemind.app.service.property

import com.hivemind.app.logging.Logger
import com.hivemind.app.model.Property
import com.hivemind.app.repository.property.PropertyRepository
import com.hivemind.app.service.exception.ServiceException
import zio.{IO, URLayer, ZIO, ZLayer}

trait PropertyService {
  def findPropertiesOfUser(userId: Int): IO[ServiceException, Set[Property]]
  def findProperty(propertyId: Int): IO[ServiceException, Option[Property]]
}

object PropertyService {

  val live: URLayer[Logger with PropertyRepository, PropertyService] =
    ZLayer { // apply == fromZIO
      for {
        logger         <- ZIO.service[Logger]
        userRepository <- ZIO.service[PropertyRepository]
      } yield PropertyServiceImpl(userRepository, logger)
    }
}
