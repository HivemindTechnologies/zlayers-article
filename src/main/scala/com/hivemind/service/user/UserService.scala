package com.hivemind.service.user

import com.hivemind.model.User
import com.hivemind.service.exception.ServiceException
import zio.IO

trait UserService {
  def findUser(id: Int): IO[ServiceException, Option[User]]
}
