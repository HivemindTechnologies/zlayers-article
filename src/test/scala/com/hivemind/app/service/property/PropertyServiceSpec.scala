package com.hivemind.app.service.property

import com.hivemind.app.config.Config
import com.hivemind.app.database.exception.DatabaseLayerExecutionOutcome
import com.hivemind.app.database.model.{PropertyRecord, UserRecord}
import com.hivemind.app.database.{Database, DatabaseImpl}
import com.hivemind.app.logging.Logger
import com.hivemind.app.model.Property
import com.hivemind.app.repository.property.{PropertyRepository, PropertyRepositoryImpl}
import com.hivemind.app.service.exception.ServiceException
import zio.*
import zio.test.*
import zio.test.Assertion.{anything, equalTo, isSubtype}

object PropertyServiceSpec extends ZIOSpecDefault {

  val test1: Spec[Any, ServiceException] = test("returns the property with id 1 when findProperty is executed") {
    val fixture = new TestConfiguration

    for {
      propertyService <- fixture.propertyServiceUIO
      property        <- propertyService.findProperty(1)
    } yield assertProperty(userId = 1, propertyId = 1, value = property)
  }

  val test2: Spec[Any, ServiceException] = test("returns the property with id 7 when findProperty is executed") {
    val fixture = new TestConfiguration

    for {
      propertyService <- fixture.propertyServiceUIO
      property        <- propertyService.findProperty(7)
    } yield assertProperty(userId = 3, propertyId = 7, value = property)
  }

  val test3: Spec[Any, ServiceException] = test("returns the properties of Alonzo Church when findPropertiesOfUser is executed") {
    val fixture = new TestConfiguration

    for {
      propertyService <- fixture.propertyServiceUIO
      properties      <- propertyService.findPropertiesOfUser(1)
    } yield assertProperties(userId = 1, propertyIds = List(1, 2, 3), value = properties)
  }

  val test4: Spec[Any, ServiceException] = test("returns the properties of Haskell Curry when findPropertiesOfUser is executed") {
    val fixture = new TestConfiguration

    for {
      propertyService <- fixture.propertyServiceUIO
      properties      <- propertyService.findPropertiesOfUser(3)
    } yield assertProperties(userId = 3, propertyIds = List(7, 8), value = properties)
  }

  val testError1: Spec[Any, Option[Property]] = test("fails with ServiceException when outcome is set to query error") {
    val fixture = new TestConfiguration {
      override lazy val outcome: DatabaseLayerExecutionOutcome = DatabaseLayerExecutionOutcome.RaiseQueryExecutionError
    }

    for {
      propertyService <- fixture.propertyServiceUIO
      error           <- propertyService.findProperty(1).flip
    } yield assert(error)(isSubtype[ServiceException](anything))
  }

  val testError2: Spec[Any, Option[Property]] = test("fails with ServiceException when outcome is set to timeout error") {
    val fixture = new TestConfiguration {
      override lazy val outcome: DatabaseLayerExecutionOutcome = DatabaseLayerExecutionOutcome.RaiseTimeoutError
    }

    for {
      propertyService <- fixture.propertyServiceUIO
      error           <- propertyService.findProperty(1).flip
    } yield assert(error)(isSubtype[ServiceException](anything))
  }

  val testError3: Spec[Any, Option[Property]] = test("fails with ServiceException when outcome is set to connection error") {
    val fixture = new TestConfiguration {
      override lazy val outcome: DatabaseLayerExecutionOutcome = DatabaseLayerExecutionOutcome.RaiseConnectionClosedError
    }

    for {
      propertyService <- fixture.propertyServiceUIO
      error           <- propertyService.findProperty(1).flip
    } yield assert(error)(isSubtype[ServiceException](anything))
  }

  def spec: Spec[TestEnvironment with Scope, Any] = suite("Property service")(test1, test2, test3, test4, testError1, testError2, testError3)

  private def assertProperty(userId: Int, propertyId: Int, value: Option[Property]): TestResult = {
    val maybePropertyRecord: Option[PropertyRecord] = DatabaseImpl.propertiesById.get(propertyId)
    val maybeUserRecord: Option[UserRecord]         = DatabaseImpl.usersById.get(userId)
    val optionProperty: Option[Property]            = PropertyRepositoryImpl.buildPropertyFromRecord(maybePropertyRecord, maybeUserRecord)

    assert(value)(equalTo(optionProperty))
  }

  private def assertProperties(userId: Int, propertyIds: List[Int], value: Set[Property]) = {
    val maybeUserRecord: Option[UserRecord] = DatabaseImpl.usersById.get(userId)

    propertyIds.foldLeft(assertTrue(true)) { (prevRes: TestResult, propertyId: Int) =>
      val maybePropertyRecord: Option[PropertyRecord] = DatabaseImpl.propertiesById.get(propertyId)
      val optionProperty: Option[Property]            = PropertyRepositoryImpl.buildPropertyFromRecord(maybePropertyRecord, maybeUserRecord)

      prevRes && assertTrue(optionProperty.isDefined) && assertTrue(value.contains(optionProperty.get))
    }
  }
}

class TestConfiguration {
  lazy val outcome: DatabaseLayerExecutionOutcome                 = DatabaseLayerExecutionOutcome.FinishWithoutErrors
  lazy val testConfig: Config                                     = Config.testConfig(outcome = outcome)
  val testConfigZLayer: ULayer[Config]                            = ZLayer.succeed(testConfig)
  val consoleZLayer: ULayer[Console.ConsoleLive.type]             = ZLayer.succeed(zio.Console.ConsoleLive)
  val propertyServiceURIO: URIO[PropertyService, PropertyService] = ZIO.service[PropertyService]

  val propertyServiceUIO: UIO[PropertyService] =
    propertyServiceURIO.provide(consoleZLayer, testConfigZLayer, Logger.live, Database.live, PropertyRepository.live, PropertyService.live)
}
