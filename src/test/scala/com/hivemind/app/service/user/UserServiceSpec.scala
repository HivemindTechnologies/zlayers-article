package com.hivemind.app.service.user

import com.hivemind.app.config.Config
import com.hivemind.app.database.exception.DatabaseLayerExecutionOutcome
import com.hivemind.app.database.model.UserRecord
import com.hivemind.app.database.{Database, DatabaseImpl}
import com.hivemind.app.logging.Logger
import com.hivemind.app.model.User
import com.hivemind.app.repository.user.{UserRepository, UserRepositoryImpl}
import com.hivemind.app.service.exception.ServiceException
import zio.*
import zio.test.*
import zio.test.Assertion.{anything, equalTo, isSubtype}

object UserServiceSpec extends ZIOSpecDefault {

  val test1: Spec[Any, ServiceException] = test("returns Alonzo Church when findUser is executed") {
    val fixture = new TestConfiguration

    for {
      userService <- fixture.userServiceUIO
      user        <- userService.findUser(1)
    } yield assertAlonzoChurch(user)
  }

  val test2: Spec[Any, ServiceException] = test("returns Alan Turing when findUser is executed") {
    val fixture = new TestConfiguration

    for {
      userService <- fixture.userServiceUIO
      user        <- userService.findUser(2)
    } yield assertAlanTuring(user)
  }

  val testError1: Spec[Any, Option[User]] = test("fails with ServiceException when outcome is set to query error") {
    val fixture = new TestConfiguration {
      override lazy val outcome: DatabaseLayerExecutionOutcome = DatabaseLayerExecutionOutcome.RaiseQueryExecutionError
    }

    for {
      userService <- fixture.userServiceUIO
      error       <- userService.findUser(1).flip
    } yield assert(error)(isSubtype[ServiceException](anything))
  }

  val testError2: Spec[Any, Option[User]] = test("fails with ServiceException when outcome is set to timeout error") {
    val fixture = new TestConfiguration {
      override lazy val outcome: DatabaseLayerExecutionOutcome = DatabaseLayerExecutionOutcome.RaiseTimeoutError
    }

    for {
      userService <- fixture.userServiceUIO
      error       <- userService.findUser(1).flip
    } yield assert(error)(isSubtype[ServiceException](anything))
  }

  val testError3: Spec[Any, Option[User]] = test("fails with ServiceException when outcome is set to connection error") {
    val fixture = new TestConfiguration {
      override lazy val outcome: DatabaseLayerExecutionOutcome = DatabaseLayerExecutionOutcome.RaiseConnectionClosedError
    }

    for {
      userService <- fixture.userServiceUIO
      error       <- userService.findUser(1).flip
    } yield assert(error)(isSubtype[ServiceException](anything))
  }

  def spec: Spec[TestEnvironment with Scope, Any] = suite("User service")(test1, test2, testError1, testError2, testError3)

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
  lazy val outcome: DatabaseLayerExecutionOutcome     = DatabaseLayerExecutionOutcome.FinishWithoutErrors
  lazy val testConfig: Config                         = Config.testConfig(outcome = outcome)
  val testConfigZLayer: ULayer[Config]                = ZLayer.succeed(testConfig)
  val consoleZLayer: ULayer[Console.ConsoleLive.type] = ZLayer.succeed(zio.Console.ConsoleLive)
  val userServiceURIO: URIO[UserService, UserService] = ZIO.service[UserService]

  val userServiceUIO: UIO[UserService] =
    userServiceURIO.provide(consoleZLayer, testConfigZLayer, Logger.live, Database.live, UserRepository.live, UserService.live)
}
