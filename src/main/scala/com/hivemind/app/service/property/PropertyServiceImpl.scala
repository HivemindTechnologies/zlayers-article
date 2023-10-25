package com.hivemind.app.service.property

import com.hivemind.app.logging.Logger
import com.hivemind.app.model.Property
import com.hivemind.app.repository.exception.{RepositoryConnectionError, RepositoryException}
import com.hivemind.app.repository.property.PropertyRepository
import com.hivemind.app.service.exception.ServiceException.handleRepositoryErrors
import com.hivemind.app.service.exception.{ServiceConnectionError, ServiceException}
import zio.{IO, ZIO}

class PropertyServiceImpl(propertyRepository: PropertyRepository, logger: Logger) extends PropertyService {
  override def findPropertiesOfUser(userId: Int): IO[ServiceException, Set[Property]] =
    for {
      list <- handleRepositoryErrors(propertyRepository.getPropertiesByOwnerId(userId = userId), logger)
    } yield list.toSet

  override def findProperty(propertyId: Int): IO[ServiceException, Option[Property]] =
    handleRepositoryErrors(propertyRepository.getPropertyById(propertyId = propertyId), logger)

}
