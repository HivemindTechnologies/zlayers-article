package com.hivemind.repository

import com.hivemind.model.User
import com.hivemind.repository.exception.RepositoryException
import zio.IO
import java.util.UUID

trait UserRepository {
  def getUserById(id: UUID): IO[RepositoryException, Option[User]]
}
