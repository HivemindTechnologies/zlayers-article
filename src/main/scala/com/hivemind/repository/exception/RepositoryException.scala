package com.hivemind.repository.exception

import java.util.UUID

sealed trait RepositoryException(message: String)

case class UserNotFound(userId: UUID)         extends RepositoryException(s"User not found: ${userId.toString}")
case class PropertyNotFound(propertyId: UUID) extends RepositoryException(s"Property not found: ${propertyId.toString}")
case class ConnectionError(message: String)   extends RepositoryException(message)
