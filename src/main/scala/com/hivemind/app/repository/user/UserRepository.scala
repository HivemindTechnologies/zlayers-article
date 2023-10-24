package com.hivemind.app.repository.user

import com.hivemind.app.database.Database
import com.hivemind.app.model.User
import com.hivemind.app.repository.exception.RepositoryException
import zio.{IO, URLayer, ZIO, ZLayer}

trait UserRepository {
  def getUserById(id: Int): IO[RepositoryException, Option[User]]
}

object UserRepository {
  val live: URLayer[Database, UserRepository] =
    ZLayer { // apply == fromZIO
      for {
        database <- ZIO.service[Database]
      } yield UserRepositoryImpl(database)
    }
}
