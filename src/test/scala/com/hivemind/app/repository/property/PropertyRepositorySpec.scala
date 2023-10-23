package com.hivemind.app.repository.property

import com.hivemind.app.config.Config
import com.hivemind.app.database.model.*
import com.hivemind.app.database.{Database, DatabaseImpl}
import com.hivemind.app.logging.Logger
import com.hivemind.app.model.Property
import com.hivemind.app.repository.exception.RepositoryException
import zio.*
import zio.test.*
import zio.test.Assertion.*

object UserRepositorySpec extends ZIOSpecDefault {

  val test1: Spec[Any, RepositoryException] = test("returns Alonzo Church when getPropertyById is executed") {
    val fixture = new TestConfiguration

    for {
      propertyRepository <- fixture.repositoryIO
      property           <- propertyRepository.getPropertyById(1)
    } yield assertAlonzoChurchCar(property)
  }

  val test2: Spec[Any, Option[Property]] = test("returns an exception when getPropertyById is executed") {
    val fixture = new TestConfiguration {
      override lazy val probabilityOfErrors: Double = 100.0
    }

    for {
      propertyRepository <- fixture.repositoryIO
      error              <- propertyRepository.getPropertyById(1).flip
    } yield assert(error)(isSubtype[RepositoryException](anything))
  }

  val test3: Spec[Any, RepositoryException] = test("returns Alan Turing when getPropertyById is executed") {
    val fixture = new TestConfiguration

    for {
      propertyRepository <- fixture.repositoryIO
      user               <- propertyRepository.getPropertyById(4)
    } yield assertAlanTuringHouse(user)
  }

  def spec: Spec[TestEnvironment with Scope, Any] = suite("Property repository")(test1, test2, test3)

  private def assertAlonzoChurchCar(value: Option[Property]): TestResult = {
    val propertyRecord: PropertyRecord   = DatabaseImpl.car1
    val userRecord: UserRecord           = DatabaseImpl.alonzoChurch
    val optionProperty: Option[Property] = PropertyRepositoryImpl.buildPropertyFromRecord(Some(propertyRecord), Some(userRecord))

    assert(value)(equalTo(optionProperty))
  }

  private def assertAlanTuringHouse(value: Option[Property]): TestResult = {
    val propertyRecord: PropertyRecord   = DatabaseImpl.house2
    val userRecord: UserRecord           = DatabaseImpl.alanTuring
    val optionProperty: Option[Property] = PropertyRepositoryImpl.buildPropertyFromRecord(Some(propertyRecord), Some(userRecord))

    assert(value)(equalTo(optionProperty))
  }
}

class TestConfiguration {
  final val neverFail: Double                                            = 0.0
  final val alwaysFail: Double                                           = 100.0
  lazy val probabilityOfErrors: Double                                   = neverFail
  lazy val testConfig: Config                                            = Config.testConfig(probabilityOfErrors)
  val testConfigZLayer: ULayer[Config]                                   = ZLayer.succeed(testConfig)
  val consoleZLayer: ULayer[Console.ConsoleLive.type]                    = ZLayer.succeed(zio.Console.ConsoleLive)
  val userRepositoryZLayer: URIO[PropertyRepository, PropertyRepository] = ZIO.service[PropertyRepository]

  val repositoryIO: UIO[PropertyRepository] =
    userRepositoryZLayer.provide(consoleZLayer, testConfigZLayer, Logger.live, Database.live, PropertyRepository.live)
}
