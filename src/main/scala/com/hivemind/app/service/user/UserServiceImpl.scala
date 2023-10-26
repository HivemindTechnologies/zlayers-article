package com.hivemind.app.service.user

import com.hivemind.app.logging.Logger
import com.hivemind.app.model.User
import com.hivemind.app.repository.user.UserRepository
import com.hivemind.app.service.exception.ServiceException
import com.hivemind.app.service.exception.ServiceException.handleRepositoryErrors
import zio.IO

class UserServiceImpl(userRepository: UserRepository, logger: Logger) extends UserService {
  override def findUser(id: Int): IO[ServiceException, Option[User]] =
    handleRepositoryErrors(userRepository.getUserById(id), logger)

}
