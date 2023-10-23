package com.hivemind.app.repository.user

import com.hivemind.app.database.Database
import com.hivemind.app.model.User
import com.hivemind.app.repository.exception.RepositoryException
import zio.{IO, ZIO, ZLayer}

trait UserRepository {
  def getUserById(id: Int): IO[RepositoryException, Option[User]]
}

object UserRepository {
  val live: ZLayer[Database, Nothing, UserRepository] = ZLayer {
    for {
      database <- ZIO.service[Database]
    } yield UserRepositoryImpl(database)
  }
}
