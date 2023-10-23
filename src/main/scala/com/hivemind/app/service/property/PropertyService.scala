package com.hivemind.app.service.property

import com.hivemind.app.model.Property
import com.hivemind.app.service.exception.ServiceException
import zio.IO

trait PropertyService {
  def findPropertiesOfUser(userId: Int): IO[ServiceException, Set[Property]]
  def findProperty(propertyId: Int): IO[ServiceException, Option[Property]]
}
