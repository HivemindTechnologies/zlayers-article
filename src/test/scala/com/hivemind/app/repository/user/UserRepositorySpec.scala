package com.hivemind.app.repository.user

import com.hivemind.app.config.Config
import com.hivemind.app.database.model.*
import com.hivemind.app.database.{Database, DatabaseImpl}
import com.hivemind.app.logging.Logger
import com.hivemind.app.model.User
import com.hivemind.app.repository.exception.RepositoryException
import zio.*
import zio.test.*
import zio.test.Assertion.*

object UserRepositorySpec extends ZIOSpecDefault {

  val test1: Spec[Any, RepositoryException] = test("returns Alonzo Church when getUserById is executed") {
    val fixture = new TestConfiguration

    for {
      userRepository <- fixture.repositoryIO
      user           <- userRepository.getUserById(1)
    } yield assertAlonzoChurch(user)
  }

  val test2: Spec[Any, Option[User]] = test("returns an exception when getUserById is executed") {
    val fixture = new TestConfiguration {
      override lazy val probabilityOfErrors: Double = 100.0
    }

    for {
      userRepository <- fixture.repositoryIO
      error          <- userRepository.getUserById(1).flip
    } yield assert(error)(isSubtype[RepositoryException](anything))
  }

  val test3: Spec[Any, RepositoryException] = test("returns Alan Turing when getUserById is executed") {
    val fixture = new TestConfiguration

    for {
      userRepository <- fixture.repositoryIO
      user           <- userRepository.getUserById(2)
    } yield assertAlanTuring(user)
  }

  def spec: Spec[TestEnvironment with Scope, Any] = suite("User repository")(test1, test2, test3)

  private def assertAlonzoChurch(value: Option[User]): TestResult = {
    val record: UserRecord       = DatabaseImpl.alonzoChurch
    val optionUser: Option[User] = UserRepositoryImpl.buildUserFromRecord(record)

    assert(value)(equalTo(optionUser))
  }

  private def assertAlanTuring(value: Option[User]): TestResult = {
    val record: UserRecord       = DatabaseImpl.alanTuring
    val optionUser: Option[User] = UserRepositoryImpl.buildUserFromRecord(record)

    assert(value)(equalTo(optionUser))
  }
}

class TestConfiguration {
  final val neverFail: Double                                    = 0.0
  final val alwaysFail: Double                                   = 100.0
  lazy val probabilityOfErrors: Double                           = neverFail
  lazy val testConfig: Config                                    = Config.testConfig(probabilityOfErrors)
  val testConfigZLayer: ULayer[Config]                           = ZLayer.succeed(testConfig)
  val consoleZLayer: ULayer[Console.ConsoleLive.type]            = ZLayer.succeed(zio.Console.ConsoleLive)
  val userRepositoryZLayer: URIO[UserRepository, UserRepository] = ZIO.service[UserRepository]

  val repositoryIO: UIO[UserRepository] =
    userRepositoryZLayer.provide(consoleZLayer, testConfigZLayer, Logger.live, Database.live, UserRepository.live)
}
