package com.hivemind.service.user

import java.util.UUID
import com.hivemind.model.User
import com.hivemind.service.exception.ServiceException
import zio.IO

trait UserService {
  def findUser(id: UUID): IO[ServiceException, Option[User]]
}
