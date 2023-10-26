package com.hivemind.app.repository.user

import com.hivemind.app.config.Config
import com.hivemind.app.database.exception.DatabaseLayerExecutionOutcome
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
      userRepository <- fixture.repositoryUIO
      user           <- userRepository.getUserById(1)
    } yield assertAlonzoChurch(user)
  }

  val test2: Spec[Any, RepositoryException] = test("returns Alan Turing when getUserById is executed") {
    val fixture = new TestConfiguration

    for {
      userRepository <- fixture.repositoryUIO
      user           <- userRepository.getUserById(2)
    } yield assertAlanTuring(user)
  }

  val testError1: Spec[Any, Option[User]] = test("fails with RepositoryException when outcome is set to query error") {
    val fixture = new TestConfiguration {
      override lazy val outcome: DatabaseLayerExecutionOutcome = DatabaseLayerExecutionOutcome.RaiseQueryExecutionError
    }

    for {
      userRepository <- fixture.repositoryUIO
      error          <- userRepository.getUserById(1).flip
    } yield assert(error)(isSubtype[RepositoryException](anything))
  }

  val testError2: Spec[Any, Option[User]] = test("fails with RepositoryException when outcome is set to connection error") {
    val fixture = new TestConfiguration {
      override lazy val outcome: DatabaseLayerExecutionOutcome = DatabaseLayerExecutionOutcome.RaiseConnectionClosedError
    }

    for {
      userRepository <- fixture.repositoryUIO
      error          <- userRepository.getUserById(1).flip
    } yield assert(error)(isSubtype[RepositoryException](anything))
  }

  val testError3: Spec[Any, Option[User]] = test("fails with RepositoryException when outcome is set to timeout error") {
    val fixture = new TestConfiguration {
      override lazy val outcome: DatabaseLayerExecutionOutcome = DatabaseLayerExecutionOutcome.RaiseTimeoutError
    }

    for {
      userRepository <- fixture.repositoryUIO
      error          <- userRepository.getUserById(1).flip
    } yield assert(error)(isSubtype[RepositoryException](anything))
  }

  def spec: Spec[TestEnvironment with Scope, Any] = suite("User repository")(test1, test2, testError1, testError2, testError3)

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
  lazy val outcome: DatabaseLayerExecutionOutcome              = DatabaseLayerExecutionOutcome.FinishWithoutErrors
  lazy val testConfig: Config                                  = Config.testConfig(outcome = outcome)
  val testConfigZLayer: ULayer[Config]                         = ZLayer.succeed(testConfig)
  val consoleZLayer: ULayer[Console.ConsoleLive.type]          = ZLayer.succeed(zio.Console.ConsoleLive)
  val userRepositoryURIO: URIO[UserRepository, UserRepository] = ZIO.service[UserRepository]

  val repositoryUIO: UIO[UserRepository] =
    userRepositoryURIO.provide(consoleZLayer, testConfigZLayer, Logger.live, Database.live, UserRepository.live)
}
