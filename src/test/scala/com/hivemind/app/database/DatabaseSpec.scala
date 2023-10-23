package com.hivemind.app.database

import com.hivemind.app.config.Config
import com.hivemind.app.database.exception.{DatabaseConnectionClosedException, DatabaseException}
import com.hivemind.app.database.model.{TableName, *}
import com.hivemind.app.logging.Logger
import zio.*
import zio.test.*
import zio.test.Assertion.*

object DatabaseSpec extends ZIOSpecDefault {

  val test1: Spec[Any, DatabaseException] = test("returns Alonzo Church when getObjectById is executed") {
    val fixture = new TestConfiguration

    for {
      db     <- fixture.databaseIO
      record <- db.getObjectById(1, TableName.Users)
    } yield assertAlonzoChurch(record)
  }

  val test2: Spec[Any, Option[Record]] = test("returns an exception when getObjectById is executed (if probability of errors is 100%)") {
    val fixture = new TestConfiguration {
      override lazy val probabilityOfErrors: Double = 100.0
    }

    for {
      db    <- fixture.databaseIO
      error <- db.getObjectById(1, TableName.Users).flip
    } yield assert(error)(isSubtype[DatabaseConnectionClosedException](anything))
  }

  val test3: Spec[Any, DatabaseException] = test("returns Alan Turing when getObjectById is executed") {
    val fixture = new TestConfiguration

    for {
      db     <- fixture.databaseIO
      record <- db.getObjectById(2, TableName.Users)
    } yield assertAlanTuring(record)
  }

  val test4: Spec[Any, DatabaseException] = test("returns property 1 when getObjectById is executed") {
    val fixture = new TestConfiguration

    for {
      db     <- fixture.databaseIO
      record <- db.getObjectById(1, TableName.Properties)
    } yield assertProperty1(record)
  }

  val test5: Spec[Any, DatabaseException] = test("returns property 2 when getObjectById is executed") {
    val fixture = new TestConfiguration

    for {
      db     <- fixture.databaseIO
      record <- db.getObjectById(2, TableName.Properties)
    } yield assertProperty2(record)
  }

  val test6: Spec[Any, DatabaseException] = test("returns list of users when getAllRecords is executed") {
    val fixture = new TestConfiguration

    for {
      db            <- fixture.databaseIO
      listOfRecords <- db.getAllRecords(TableName.Users)
    } yield assertListOfUsers(listOfRecords)
  }

  val test7: Spec[Any, DatabaseException] = test("returns list of properties when getAllRecords is executed") {
    val fixture = new TestConfiguration

    for {
      db            <- fixture.databaseIO
      listOfRecords <- db.getAllRecords(TableName.Properties)
    } yield assertListOfProperties(listOfRecords)
  }

  def spec: Spec[TestEnvironment with Scope, Any] = suite("Database implementation")(test1, test2, test3, test4, test5, test6, test7)

  private def assertAlonzoChurch(value: Option[Record]): TestResult =
    assert(value)(isSome(equalTo(DatabaseImpl.alonzoChurch)))

  private def assertAlanTuring(value: Option[Record]): TestResult =
    assert(value)(isSome(equalTo(DatabaseImpl.alanTuring)))

  private def assertProperty1(value: Option[Record]): TestResult =
    assert(value)(isSome(equalTo(DatabaseImpl.properties.head)))

  private def assertProperty2(value: Option[Record]): TestResult =
    assert(value)(isSome(equalTo(DatabaseImpl.properties(1))))

  private def assertListOfUsers(values: List[Record]): TestResult =
    assert(values)(hasSize(equalTo(3))) &&
    assert(values)(hasAt(0)(equalTo(DatabaseImpl.alonzoChurch))) &&
    assert(values)(hasAt(1)(equalTo(DatabaseImpl.alanTuring))) &&
    assert(values)(hasAt(2)(equalTo(DatabaseImpl.haskellCurry)))

  private def assertListOfProperties(values: List[Record]): TestResult =
    assert(values)(hasSize(equalTo(8))) &&
    assert(values)(equalTo(DatabaseImpl.properties))

}

class TestConfiguration {
  final val neverFail: Double                         = 0.0
  final val alwaysFail: Double                        = 100.0
  lazy val probabilityOfErrors: Double                = neverFail
  lazy val testConfig: Config                         = Config.testConfig(probabilityOfErrors)
  val testConfigZLayer: ULayer[Config]                = ZLayer.succeed(testConfig)
  val consoleZLayer: ULayer[Console.ConsoleLive.type] = ZLayer.succeed(zio.Console.ConsoleLive)
  val dbTest: URIO[Database, Database]                = ZIO.service[Database]

  val databaseIO: UIO[Database] = dbTest.provide(consoleZLayer, testConfigZLayer, Logger.live, Database.live)
}
