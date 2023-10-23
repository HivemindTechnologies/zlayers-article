//package com.hivemind.app.service.property
//
//import com.hivemind.app.config.Config
//import com.hivemind.app.database.model.UserRecord
//import com.hivemind.app.database.{Database, DatabaseImpl}
//import com.hivemind.app.logging.Logger
//import com.hivemind.app.model.User
//import com.hivemind.app.repository.user.{UserRepository, UserRepositoryImpl}
//import com.hivemind.app.service.exception.ServiceException
//import zio.*
//import zio.test.*
//import zio.test.Assertion.{anything, equalTo, isSubtype}
//
//object UserServiceSpec extends ZIOSpecDefault {
//
//  val test1: Spec[Any, ServiceException] = test("returns Alonzo Church when findUser is executed") {
//    val fixture = new TestConfiguration
//
//    for {
//      userService <- fixture.userServiceIO
//      user        <- userService.findUser(1)
//    } yield assertAlonzoChurch(user)
//  }
//
//  val test2: Spec[Any, Option[User]] = test("returns an exception when findUser is executed  (if probability of errors is 100%)") {
//    val fixture = new TestConfiguration {
//      override lazy val probabilityOfErrors: Double = 100.0
//    }
//
//    for {
//      userService <- fixture.userServiceIO
//      error       <- userService.findUser(1).flip
//    } yield assert(error)(isSubtype[ServiceException](anything))
//  }
//
//  val test3: Spec[Any, ServiceException]          = test("returns Alan Turing when findUser is executed") {
//    val fixture = new TestConfiguration
//
//    for {
//      userService <- fixture.userServiceIO
//      user        <- userService.findUser(2)
//    } yield assertAlanTuring(user)
//  }
//  def spec: Spec[TestEnvironment with Scope, Any] = suite("User service")(test1, test2, test3)
//
//  private def assertAlonzoChurch(value: Option[User]): TestResult = {
//    val record: UserRecord       = DatabaseImpl.alonzoChurch
//    val optionUser: Option[User] = UserRepositoryImpl.buildUserFromRecord(record)
//
//    assert(value)(equalTo(optionUser))
//  }
//
//  private def assertAlanTuring(value: Option[User]): TestResult = {
//    val record: UserRecord       = DatabaseImpl.alanTuring
//    val optionUser: Option[User] = UserRepositoryImpl.buildUserFromRecord(record)
//
//    assert(value)(equalTo(optionUser))
//  }
//
//}
//
//class TestConfiguration {
//  final val neverFail: Double                                 = 0.0
//  final val alwaysFail: Double                                = 100.0
//  lazy val probabilityOfErrors: Double                        = neverFail
//  lazy val testConfig: Config                                 = Config.testConfig(probabilityOfErrors)
//  val testConfigZLayer: ULayer[Config]                        = ZLayer.succeed(testConfig)
//  val consoleZLayer: ULayer[Console.ConsoleLive.type]         = ZLayer.succeed(zio.Console.ConsoleLive)
//  val propertyService: URIO[PropertyService, PropertyService] = ZIO.service[PropertyService]
//
//  val userServiceIO: UIO[PropertyService] =
//    propertyService.provide(consoleZLayer, testConfigZLayer, Logger.live, Database.live, UserRepository.live, PropertyService.live)
//}
