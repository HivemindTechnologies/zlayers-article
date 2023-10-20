package com.hivemind.app.database

import com.hivemind.app.config.{Config, DatabaseParameters}
import com.hivemind.app.database.exception.{DatabaseConnectionClosedException, DatabaseException}
import com.hivemind.app.database.model.{TableName, *}
import com.hivemind.app.logging.Logger
import zio.*
import zio.test.*
import zio.test.Assertion.*

import java.io.IOException

object DatabaseSpec extends ZIOSpecDefault {

  val helloWorldTest: Spec[Any, IOException] = test("sayHello correctly displays output") {
    for {
      _      <- Console.printLine("Hello, World!")
      output <- TestConsole.output
    } yield assertTrue(output == Vector("Hello, World!\n"))
  }

  val test1: Spec[Any, DatabaseException] = test("returns data when getObjectById(1, TableName.Users) is executed") {
    val fixture = new Fixture

    val maybeRecordIO: IO[DatabaseException, Option[Record]] = for {
      db     <- fixture.databaseIO
      record <- db.getObjectById(1, TableName.Users)
    } yield record

    maybeRecordIO.map { (maybeRecord: Option[Record]) =>
      val maybeResult: Option[TestResult] = for {
        record     <- maybeRecord
        userRecord <- record.toUserRecord
      } yield assertTrue(userRecord.name == "Alonzo")

      assert(maybeResult)(isSome)
      maybeResult.get
    }
  }

  val test2: Spec[Any, DatabaseException] = test("returns an exception getObjectById(1, TableName.Users) when is executed") {
    val fixture = new Fixture {
      override lazy val probability = alwaysFail
    }

    val maybeRecordIO: IO[DatabaseException, Option[Record]] = for {
      db     <- fixture.databaseIO
      record <- db.getObjectById(1, TableName.Users)
    } yield record

    // FIXME: Continue to compile this assertion
    assert(maybeRecordIO)(throws(isSubtype[DatabaseConnectionClosedException](anything)))
  }

  def spec = suite("Database implementation")(helloWorldTest, test1, test2)
}

class Fixture {
  lazy val probability   = neverFails
  lazy val testConfig    = Config(DatabaseParameters("myDB", "password", 5, probabilityOfError = probability))
  val neverFails: Double = 0.0
  val alwaysFail: Double = 100.0
  val testConfigZLayer   = ZLayer.succeed(testConfig)
  val consoleZLayer      = ZLayer.succeed(zio.Console.ConsoleLive)
  val dbLive             = for {
    db <- ZIO.service[Database]
  } yield db

  val databaseIO: UIO[Database] = dbLive.provide(consoleZLayer, testConfigZLayer, Logger.live, Database.live)
}
