package com.hivemind.app.repository.property

import com.hivemind.app.database.Database
import com.hivemind.app.model.Property
import com.hivemind.app.repository.exception.RepositoryException
import zio.{IO, ZIO, ZLayer}

trait PropertyRepository {
  def getPropertyById(propertyId: Int): IO[RepositoryException, Option[Property]]
  def getPropertiesByOwnerId(userId: Int): IO[RepositoryException, List[Property]]
}

object PropertyRepository {
  val live: ZLayer[Database, Nothing, PropertyRepository] = ZLayer {
    for {
      database <- ZIO.service[Database]
    } yield PropertyRepositoryImpl(database)
  }
}
