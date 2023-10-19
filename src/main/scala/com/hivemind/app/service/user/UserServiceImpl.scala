package com.hivemind.app.service.user

import com.hivemind.app.model.User
import com.hivemind.app.repository.UserRepository
import com.hivemind.app.repository.exception.RepositoryConnectionError
import com.hivemind.app.service.exception.{ServiceConnectionError, ServiceException}
import zio.IO

class UserServiceImpl(userRepository: UserRepository) extends UserService {
  override def findUser(id: Int): IO[ServiceException, Option[User]] =
    userRepository.getUserById(id).mapError { case RepositoryConnectionError =>
      ServiceConnectionError
    }
}
