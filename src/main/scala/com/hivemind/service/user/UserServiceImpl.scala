package com.hivemind.service.user

import com.hivemind.model.User
import com.hivemind.repository.UserRepository
import com.hivemind.service.exception.{ServiceException, UnknownError, UserNotFound}
import com.hivemind.repository.exception.{RepositoryException, UserNotFound as UserNotFoundInRepo}
import zio.IO
import java.util.UUID

class UserServiceImpl(userRepository: UserRepository) extends UserService {
  override def findUser(id: UUID): IO[ServiceException, Option[User]] =
    userRepository.getUserById(id).mapError(mapRepositoryToServiceError)

  private def mapRepositoryToServiceError(error: RepositoryException): ServiceException = error match {
    case UserNotFoundInRepo(userId) =>
      UserNotFound(userId)
    case _                          =>
      UnknownError("An unexpected error occurred while retrieving user")
  }
}
