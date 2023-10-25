package com.hivemind.app.service.user

import com.hivemind.app.logging.Logger
import com.hivemind.app.model.User
import com.hivemind.app.repository.user.UserRepository
import com.hivemind.app.service.exception.ServiceException
import zio.{IO, URLayer, ZIO, ZLayer}

trait UserService {
  def findUser(id: Int): IO[ServiceException, Option[User]]
}

object UserService {
  val live: URLayer[Logger with UserRepository, UserService] =
    ZLayer { // apply == fromZIO
      for {
        logger         <- ZIO.service[Logger]
        userRepository <- ZIO.service[UserRepository]
      } yield UserServiceImpl(userRepository, logger)
    }
}
