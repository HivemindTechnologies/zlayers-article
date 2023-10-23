package com.hivemind.app.service.user

import com.hivemind.app.model.User
import com.hivemind.app.repository.user.UserRepository
import com.hivemind.app.service.exception.ServiceException
import zio.{IO, ZIO, ZLayer}

trait UserService {
  def findUser(id: Int): IO[ServiceException, Option[User]]
}

object UserService {
  val live: ZLayer[UserRepository, Nothing, UserService] =
    ZLayer { // apply == fromZIO
      for {
        userRepository <- ZIO.service[UserRepository]
      } yield UserServiceImpl(userRepository)
    }
}
