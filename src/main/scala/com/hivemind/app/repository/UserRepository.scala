package com.hivemind.app.repository

import com.hivemind.app.model.User
import com.hivemind.app.repository.exception.RepositoryException
import zio.IO

trait UserRepository {
  def getUserById(id: Int): IO[RepositoryException, Option[User]]
}
