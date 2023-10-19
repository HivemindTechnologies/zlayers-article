package com.hivemind.repository

import com.hivemind.model.User
import com.hivemind.repository.exception.RepositoryException
import zio.IO

trait UserRepository {
  def getUserById(id: Int): IO[RepositoryException, Option[User]]
}
