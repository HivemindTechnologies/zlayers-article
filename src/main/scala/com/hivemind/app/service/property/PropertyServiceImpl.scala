package com.hivemind.app.service.property

import com.hivemind.app.model.Property
import com.hivemind.app.repository.exception.RepositoryConnectionError
import com.hivemind.app.repository.property.PropertyRepository
import com.hivemind.app.service.exception.{ServiceConnectionError, ServiceException}
import zio.IO

class PropertyServiceImpl(propertyRepository: PropertyRepository) extends PropertyService {
  override def findPropertiesOfUser(userId: Int): IO[ServiceException, Set[Property]] =
    for {
      list <- propertyRepository.getPropertiesByOwnerId(userId = userId).mapError { case RepositoryConnectionError => ServiceConnectionError }
    } yield list.toSet

  override def findProperty(propertyId: Int): IO[ServiceException, Option[Property]] =
    propertyRepository.getPropertyById(propertyId = propertyId).mapError { case RepositoryConnectionError =>
      ServiceConnectionError
    }
}
