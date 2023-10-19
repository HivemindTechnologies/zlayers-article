package com.hivemind.app.service.property

import com.hivemind.app.model.Property
import com.hivemind.app.service.exception.ServiceException
import zio.IO
import java.util.UUID

trait PropertyService {
  def findPropertiesOfUser(userId: UUID): IO[ServiceException, Set[Property]]
  def findProperty(propertyId: UUID): IO[ServiceException, Option[Property]]
}
