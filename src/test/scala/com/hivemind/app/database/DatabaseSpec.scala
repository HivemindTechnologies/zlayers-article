package com.hivemind.app.database

import com.hivemind.app.config.Config
import com.hivemind.app.database.exception.*
import com.hivemind.app.database.model.{Record, TableName}
import com.hivemind.app.logging.Logger
import zio.*
import zio.test.*
import zio.test.Assertion.*

object DatabaseSpec extends ZIOSpecDefault {

  val test1: Spec[Scope, DatabaseException] = test("returns Alonzo Church record when getObjectById is executed") {
    val fixture = new TestConfiguration

    for {
      db     <- fixture.databaseURIO
      record <- db.getObjectById(1, TableName.Users)
    } yield assertAlonzoChurch(record)
  }

  val test2: Spec[Scope, DatabaseException] = test("returns Alan Turing record when getObjectById is executed") {
    val fixture = new TestConfiguration

    for {
      db     <- fixture.databaseURIO
      record <- db.getObjectById(2, TableName.Users)
    } yield assertAlanTuring(record)
  }

  val test3: Spec[Scope, DatabaseException] = test("returns property with id=1 record when getObjectById is executed") {
    val fixture = new TestConfiguration

    for {
      db     <- fixture.databaseURIO
      record <- db.getObjectById(1, TableName.Properties)
    } yield assertProperty1(record)
  }

  val test4: Spec[Scope, DatabaseException] = test("returns property with id=2 record when getObjectById is executed") {
    val fixture = new TestConfiguration

    for {
      db     <- fixture.databaseURIO
      record <- db.getObjectById(2, TableName.Properties)
    } yield assertProperty2(record)
  }

  val test5: Spec[Scope, DatabaseException] = test("returns list of user records when getAllRecords is executed") {
    val fixture = new TestConfiguration

    for {
      db            <- fixture.databaseURIO
      listOfRecords <- db.getAllRecords(TableName.Users)
    } yield assertListOfUsers(listOfRecords)
  }

  val test6: Spec[Scope, DatabaseException] = test("returns list of property records when getAllRecords is executed") {
    val fixture = new TestConfiguration

    for {
      db            <- fixture.databaseURIO
      listOfRecords <- db.getAllRecords(TableName.Properties)
    } yield assert(listOfRecords)(hasSize(equalTo(8)))
  }

  val testError1: Spec[Scope, Option[Record]] = test("fails with DatabaseConnectionClosedException when outcome is set to connection error") {
    val fixture = new TestConfiguration {
      override lazy val outcome: DatabaseLayerExecutionOutcome = DatabaseLayerExecutionOutcome.RaiseConnectionClosedError
    }

    for {
      db    <- fixture.databaseURIO
      error <- db.getObjectById(1, TableName.Users).flip
    } yield assert(error)(isSubtype[DatabaseConnectionClosedException](anything))
  }

  val testError2: Spec[Scope, Option[Record]] = test("fails with DatabaseQueryExecutionException when outcome is set to query error") {
    val fixture = new TestConfiguration {
      override lazy val outcome: DatabaseLayerExecutionOutcome = DatabaseLayerExecutionOutcome.RaiseQueryExecutionError
    }

    for {
      db    <- fixture.databaseURIO
      error <- db.getObjectById(1, TableName.Users).flip
    } yield assert(error)(isSubtype[DatabaseQueryExecutionException](anything))
  }

  val testError3: Spec[Scope, Option[Record]] = test("fails with DatabaseTimeoutException when outcome is set to timeout error") {
    val fixture = new TestConfiguration {
      override lazy val outcome: DatabaseLayerExecutionOutcome = DatabaseLayerExecutionOutcome.RaiseTimeoutError
    }

    for {
      db    <- fixture.databaseURIO
      error <- db.getObjectById(1, TableName.Users).flip
    } yield assert(error)(isSubtype[DatabaseTimeoutException](anything))
  }

  def spec: Spec[TestEnvironment with Scope, Any] =
    suite("Database implementation")(test1, test2, test3, test4, test5, test6, testError1, testError2, testError3)

  private def assertAlonzoChurch(value: Option[Record]): TestResult =
    assert(value)(isSome(equalTo(DatabaseImpl.alonzoChurch)))

  private def assertAlanTuring(value: Option[Record]): TestResult =
    assert(value)(isSome(equalTo(DatabaseImpl.alanTuring)))

  private def assertProperty1(value: Option[Record]): TestResult =
    assert(value)(equalTo(DatabaseImpl.propertiesById.get(1)))

  private def assertProperty2(value: Option[Record]): TestResult =
    assert(value)(equalTo(DatabaseImpl.propertiesById.get(2)))

  private def assertListOfUsers(values: List[Record]): TestResult =
    assert(values)(hasSize(equalTo(3))) &&
    assert(values)(hasAt(0)(equalTo(DatabaseImpl.alonzoChurch))) &&
    assert(values)(hasAt(1)(equalTo(DatabaseImpl.alanTuring))) &&
    assert(values)(hasAt(2)(equalTo(DatabaseImpl.haskellCurry)))
}

class TestConfiguration {
  lazy val outcome: DatabaseLayerExecutionOutcome = DatabaseLayerExecutionOutcome.FinishWithoutErrors
  lazy val testConfig: Config                     = Config.testConfig(outcome = outcome)
  val testConfigZLayer: ULayer[Config]            = ZLayer.succeed(testConfig)
  val consoleZLayer: ULayer[Console]              = ZLayer.succeed(Console.ConsoleLive)
  val dbServiceURIO: URIO[Database, Database]     = ZIO.service[Database]

  val databaseURIO: URIO[Scope, Database] =
    dbServiceURIO.provide(consoleZLayer, testConfigZLayer, Logger.live, Database.live)
    // To use the counter logger implementation, uncomment the next line (and comment the previous one):
    // dbTestURIO.provideSome[Scope](consoleZLayer, testConfigZLayer, Logger.liveWithLineCounter, Database.live)
}
