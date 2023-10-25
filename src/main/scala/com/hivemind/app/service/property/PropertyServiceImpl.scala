package com.hivemind.app.service.property

import com.hivemind.app.logging.Logger
import com.hivemind.app.model.Property
import com.hivemind.app.repository.exception.{RepositoryConnectionError, RepositoryException}
import com.hivemind.app.repository.property.PropertyRepository
import com.hivemind.app.service.exception.{ServiceConnectionError, ServiceException}
import zio.{IO, ZIO}

class PropertyServiceImpl(propertyRepository: PropertyRepository, serviceLogger: Logger) extends PropertyService {
  override def findPropertiesOfUser(userId: Int): IO[ServiceException, Set[Property]] =
    for {
      list <- handleRepositoryErrors(propertyRepository.getPropertiesByOwnerId(userId = userId))
    } yield list.toSet

  private def handleRepositoryErrors[A](zio: IO[RepositoryException, A]): IO[ServiceException, A] =
    zio.mapError { case RepositoryConnectionError =>
      ServiceConnectionError
    }.catchSome { case s: ServiceException =>
      s.logError() *>
        ZIO.fail(s)
    }

  override def findProperty(propertyId: Int): IO[ServiceException, Option[Property]] =
    handleRepositoryErrors(propertyRepository.getPropertyById(propertyId = propertyId))
}
