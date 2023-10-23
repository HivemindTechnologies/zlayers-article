package com.hivemind.app.database

import com.hivemind.app.config.{Config, DatabaseParameters}
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

  val test2: Spec[Any, Option[Record]] = test("returns an exception when getObjectById is executed") {
    val fixture2 = new TestConfiguration {
      override lazy val probabilityOfErrors: Double = 100.0
    }

    for {
      db    <- fixture2.databaseIO
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

  val test6: Spec[Any, DatabaseException] = test("returns list of users when executeQuery is executed") {
    val fixture = new TestConfiguration

    for {
      db            <- fixture.databaseIO
      listOfRecords <- db.executeQuery(TableName.Users)
    } yield assertListOfUsers(listOfRecords)
  }

  val test7: Spec[Any, DatabaseException] = test("returns list of properties when executeQuery is executed") {
    val fixture = new TestConfiguration

    for {
      db            <- fixture.databaseIO
      listOfRecords <- db.executeQuery(TableName.Properties)
    } yield assertListOfProperties(listOfRecords)
  }

  def spec: Spec[TestEnvironment with Scope, Any] = suite("Database implementation")(test1, test2, test3, test4, test5, test6, test7)

  private def assertAlonzoChurch(value: Option[Record]): TestResult =
    assert(value)(isSome(equalTo(UserRecord(id = 1, name = "Alonzo", surname = "Church", age = 33))))

  private def assertAlanTuring(value: Option[Record]): TestResult =
    assert(value)(isSome(equalTo(UserRecord(id = 2, name = "Alan", surname = "Turing", age = 31))))

  private def assertProperty1(value: Option[Record]): TestResult =
    assert(value)(isSome(equalTo(PropertyRecord(id = 1, propertyType = "Car", price = 17800, owner = 1))))

  private def assertProperty2(value: Option[Record]): TestResult =
    assert(value)(isSome(equalTo(PropertyRecord(id = 2, propertyType = "House", price = 230500, owner = 1))))

  private def assertListOfUsers(values: List[Record]): TestResult =
    assert(values)(hasSize(equalTo(3))) &&
    assert(values)(hasAt(0)(equalTo(UserRecord(id = 1, name = "Alonzo", surname = "Church", age = 33)))) &&
    assert(values)(hasAt(1)(equalTo(UserRecord(id = 2, name = "Alan", surname = "Turing", age = 31)))) &&
    assert(values)(hasAt(2)(equalTo(UserRecord(id = 3, name = "Haskell", surname = "Curry", age = 46))))

  private def assertListOfProperties(values: List[Record]): TestResult =
    assert(values)(hasSize(equalTo(3))) &&
    assert(values)(hasAt(0)(equalTo(PropertyRecord(id = 1, propertyType = "Car", price = 17800, owner = 1)))) &&
    assert(values)(hasAt(1)(equalTo(PropertyRecord(id = 2, propertyType = "House", price = 230500, owner = 1)))) &&
    assert(values)(hasAt(2)(equalTo(PropertyRecord(id = 3, propertyType = "Boat", price = 180000, owner = 1))))

}

class TestConfiguration {
  lazy val probabilityOfErrors: Double                = neverFail
  lazy val testConfig: Config                         = Config(DatabaseParameters("myDB", "password", 5, probabilityOfError = probabilityOfErrors))
  val neverFail: Double                               = 0.0
  val alwaysFail: Double                              = 100.0
  val testConfigZLayer: ULayer[Config]                = ZLayer.succeed(testConfig)
  val consoleZLayer: ULayer[Console.ConsoleLive.type] = ZLayer.succeed(zio.Console.ConsoleLive)
  val dbLive: ZIO[Database, Nothing, Database]        = for {
    db <- ZIO.service[Database]
  } yield db

  val databaseIO: UIO[Database] = dbLive.provide(consoleZLayer, testConfigZLayer, Logger.live, Database.live)
}
