package com.hivemind.service.exception

import java.util.UUID

sealed trait ServiceException(message: String)

case class UserNotFound(userId: UUID)         extends ServiceException(s"User not found: ${userId.toString}")
case class PropertyNotFound(propertyId: UUID) extends ServiceException(s"Property not found: ${propertyId.toString}")
case class ConnectionError(message: String)   extends ServiceException(message)
case class UnknownError(message: String)      extends ServiceException(message)
