package com.hivemind.service.user

import com.hivemind.model.User
import com.hivemind.repository.UserRepository
import com.hivemind.repository.exception.RepositoryConnectionError
import com.hivemind.service.exception.{ServiceConnectionError, ServiceException}
import zio.IO

class UserServiceImpl(userRepository: UserRepository) extends UserService {
  override def findUser(id: Int): IO[ServiceException, Option[User]] =
    userRepository.getUserById(id).mapError { case RepositoryConnectionError =>
      ServiceConnectionError
    }
}
