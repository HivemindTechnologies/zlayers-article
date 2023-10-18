package com.hivemind.service.property

import com.hivemind.model.Property
import com.hivemind.service.exception.ServiceException
import zio.IO
import java.util.UUID

trait PropertyService {
  def findPropertiesOfUser(userId: UUID): IO[ServiceException, Set[Property]]
  def findProperty(propertyId: UUID): IO[ServiceException, Option[Property]]
}
