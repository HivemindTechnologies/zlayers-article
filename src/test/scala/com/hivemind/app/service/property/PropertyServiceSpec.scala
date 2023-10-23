package com.hivemind.app.service.property

import com.hivemind.app.config.Config
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
      propertyService <- fixture.propertyServiceIO
      property        <- propertyService.findProperty(1)
    } yield assertProperty(userId = 1, propertyId = 1, value = property)
  }

  val test2: Spec[Any, Option[Property]] = test("returns an exception when findProperty is executed  (if probability of errors is 100%)") {
    val fixture = new TestConfiguration {
      override lazy val probabilityOfErrors: Double = 100.0
    }

    for {
      propertyService <- fixture.propertyServiceIO
      error           <- propertyService.findProperty(1).flip
    } yield assert(error)(isSubtype[ServiceException](anything))
  }

  val test3: Spec[Any, ServiceException] = test("returns the property with id 7 when findProperty is executed") {
    val fixture = new TestConfiguration

    for {
      propertyService <- fixture.propertyServiceIO
      property        <- propertyService.findProperty(7)
    } yield assertProperty(userId = 3, propertyId = 7, value = property)
  }

  val test4: Spec[Any, ServiceException] = test("returns the properties of Alonzo Church when findPropertiesOfUser is executed") {
    val fixture = new TestConfiguration

    for {
      propertyService <- fixture.propertyServiceIO
      properties      <- propertyService.findPropertiesOfUser(1)
    } yield assertProperties(userId = 1, propertyIds = List(1, 2, 3), value = properties)
  }

  val test5: Spec[Any, ServiceException] = test("returns the properties of Haskell Curry when findPropertiesOfUser is executed") {
    val fixture = new TestConfiguration

    for {
      propertyService <- fixture.propertyServiceIO
      properties      <- propertyService.findPropertiesOfUser(3)
    } yield assertProperties(userId = 3, propertyIds = List(7, 8), value = properties)
  }

  def spec: Spec[TestEnvironment with Scope, Any] = suite("Property service")(test1, test2, test3, test4, test5)

  private def assertProperty(userId: Int, propertyId: Int, value: Option[Property]): TestResult = {
    val propertyRecord: PropertyRecord   = DatabaseImpl.propertiesById(propertyId)
    val userRecord: UserRecord           = DatabaseImpl.usersById(userId)
    val optionProperty: Option[Property] = PropertyRepositoryImpl.buildPropertyFromRecord(Some(propertyRecord), Some(userRecord))

    assert(value)(equalTo(optionProperty))
  }

  private def assertProperties(userId: Int, propertyIds: List[Int], value: Set[Property]) = {
    val userRecord: UserRecord = DatabaseImpl.usersById(userId)

    propertyIds.foldLeft(assertTrue(true)) { (prevRes: TestResult, propertyId: Int) =>
      val propertyRecord: PropertyRecord   = DatabaseImpl.propertiesById(propertyId)
      val optionProperty: Option[Property] = PropertyRepositoryImpl.buildPropertyFromRecord(Some(propertyRecord), Some(userRecord))

      prevRes && assertTrue(optionProperty.isDefined) && assertTrue(value.contains(optionProperty.get))
    }
  }
}

class TestConfiguration {
  final val neverFail: Double                                 = 0.0
  final val alwaysFail: Double                                = 100.0
  lazy val probabilityOfErrors: Double                        = neverFail
  lazy val testConfig: Config                                 = Config.testConfig(probabilityOfErrors)
  val testConfigZLayer: ULayer[Config]                        = ZLayer.succeed(testConfig)
  val consoleZLayer: ULayer[Console.ConsoleLive.type]         = ZLayer.succeed(zio.Console.ConsoleLive)
  val propertyService: URIO[PropertyService, PropertyService] = ZIO.service[PropertyService]

  val propertyServiceIO: UIO[PropertyService] =
    propertyService.provide(consoleZLayer, testConfigZLayer, Logger.live, Database.live, PropertyRepository.live, PropertyService.live)
}
