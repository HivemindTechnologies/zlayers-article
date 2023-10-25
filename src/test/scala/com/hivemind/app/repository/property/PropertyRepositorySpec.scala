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
      propertyRepository <- fixture.repositoryUIO
      property           <- propertyRepository.getPropertyById(1)
    } yield assertAlonzoChurchCar(property)
  }

  val test2: Spec[Any, Option[Property]] = test("returns an exception when getPropertyById is executed (if probability of errors is 100%)") {
    val fixture = new TestConfiguration {
      override lazy val probabilityOfErrors: Double = 100.0
    }

    for {
      propertyRepository <- fixture.repositoryUIO
      error              <- propertyRepository.getPropertyById(1).flip
    } yield assert(error)(isSubtype[RepositoryException](anything))
  }

  val test3: Spec[Any, RepositoryException] = test("returns Alan Turing when getPropertyById is executed") {
    val fixture = new TestConfiguration

    for {
      propertyRepository <- fixture.repositoryUIO
      user               <- propertyRepository.getPropertyById(4)
    } yield assertAlanTuringHouse(user)
  }

  val test4: Spec[Any, RepositoryException] = test("returns all properties of Alan Turing when getPropertyByOwnerId is executed") {
    val fixture = new TestConfiguration

    for {
      propertyRepository <- fixture.repositoryUIO
      properties         <- propertyRepository.getPropertiesByOwnerId(2)
    } yield assertAllPropertiesOfAlanTuring(properties)
  }

  val test5: Spec[Any, RepositoryException] = test("returns all properties of Haskell Curry when getPropertyByOwnerId is executed") {
    val fixture = new TestConfiguration

    for {
      propertyRepository <- fixture.repositoryUIO
      properties         <- propertyRepository.getPropertiesByOwnerId(3)
    } yield assertAllPropertiesOfHaskellCurry(properties)
  }

  def spec: Spec[TestEnvironment with Scope, Any] = suite("Property repository")(test1, test2, test3, test4, test5)

  private def assertAlonzoChurchCar(value: Option[Property]): TestResult = {
    val maybePropertyRecord: Option[PropertyRecord] = DatabaseImpl.propertiesById.get(1)
    val userRecord: UserRecord                      = DatabaseImpl.alonzoChurch

    val optionProperty: Option[Property] = PropertyRepositoryImpl.buildPropertyFromRecord(maybePropertyRecord, Some(userRecord))

    assert(value)(equalTo(optionProperty))
  }

  private def assertAlanTuringHouse(value: Option[Property]): TestResult = {
    val maybePropertyRecord: Option[PropertyRecord] = DatabaseImpl.propertiesById.get(4)
    val userRecord: UserRecord                      = DatabaseImpl.alanTuring
    val optionProperty: Option[Property]            = PropertyRepositoryImpl.buildPropertyFromRecord(maybePropertyRecord, Some(userRecord))

    assert(value)(equalTo(optionProperty))
  }

  private def assertAllPropertiesOfAlanTuring(values: List[Property]): TestResult = {
    val alanTuringSomeUserRecord: Option[UserRecord] = Some(DatabaseImpl.alanTuring)

    val maybePropertyRecord1: Option[PropertyRecord] = DatabaseImpl.propertiesById.get(4)
    val maybeProperty1: Option[Property]             =
      PropertyRepositoryImpl.buildPropertyFromRecord(maybePropertyRecord1, alanTuringSomeUserRecord)

    val maybePropertyRecord2: Option[PropertyRecord] = DatabaseImpl.propertiesById.get(5)
    val maybeProperty2: Option[Property]             =
      PropertyRepositoryImpl.buildPropertyFromRecord(maybePropertyRecord2, alanTuringSomeUserRecord)

    val maybePropertyRecord3: Option[PropertyRecord] = DatabaseImpl.propertiesById.get(6)
    val maybeProperty3: Option[Property]             =
      PropertyRepositoryImpl.buildPropertyFromRecord(maybePropertyRecord3, alanTuringSomeUserRecord)

    (maybeProperty1, maybeProperty2, maybeProperty3) match {
      case (Some(property1), Some(property2), Some(property3)) =>
        assert(values)(hasSize(equalTo(3))) &&
        assert(values)(hasAt(0)(equalTo(property1))) &&
        assert(values)(hasAt(1)(equalTo(property2))) &&
        assert(values)(hasAt(2)(equalTo(property3)))
      case _                                                   =>
        failedTestResult
    }
  }

  private def failedTestResult: TestResult =
    assertTrue(false) // fail test

  private def assertAllPropertiesOfHaskellCurry(values: List[Property]): TestResult = {
    val haskellCurrySomeUserRecord: Option[UserRecord] = Some(DatabaseImpl.haskellCurry)

    val maybePropertyRecord1: Option[PropertyRecord] = DatabaseImpl.propertiesById.get(7)
    val maybeProperty1: Option[Property]             =
      PropertyRepositoryImpl.buildPropertyFromRecord(maybePropertyRecord1, haskellCurrySomeUserRecord)

    val maybePropertyRecord2: Option[PropertyRecord] = DatabaseImpl.propertiesById.get(8)
    val maybeProperty2: Option[Property]             =
      PropertyRepositoryImpl.buildPropertyFromRecord(maybePropertyRecord2, haskellCurrySomeUserRecord)

    (maybeProperty1, maybeProperty2) match {
      case (Some(property1), Some(property2)) =>
        assert(values)(hasSize(equalTo(2))) &&
        assert(values)(hasAt(0)(equalTo(property1))) &&
        assert(values)(hasAt(1)(equalTo(property2)))
      case _                                  =>
        failedTestResult
    }
  }
}

class TestConfiguration {
  final val neverFail: Double                                          = 0.0
  final val alwaysFail: Double                                         = 100.0
  lazy val probabilityOfErrors: Double                                 = neverFail
  lazy val testConfig: Config                                          = Config.testConfig(probabilityOfErrors)
  val testConfigZLayer: ULayer[Config]                                 = ZLayer.succeed(testConfig)
  val consoleZLayer: ULayer[Console.ConsoleLive.type]                  = ZLayer.succeed(zio.Console.ConsoleLive)
  val userRepositoryURIO: URIO[PropertyRepository, PropertyRepository] = ZIO.service[PropertyRepository]

  val repositoryUIO: UIO[PropertyRepository] =
    userRepositoryURIO.provide(consoleZLayer, testConfigZLayer, Logger.live, Database.live, PropertyRepository.live)
}
