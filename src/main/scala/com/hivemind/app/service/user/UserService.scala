package com.hivemind.app.service.user

import com.hivemind.app.model.User
import com.hivemind.app.service.exception.ServiceException
import zio.IO

trait UserService {
  def findUser(id: Int): IO[ServiceException, Option[User]]
}
