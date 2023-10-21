package com.hivemind.app.database

import com.hivemind.app.config.{Config, DatabaseParameters}
import com.hivemind.app.database.exception.{DatabaseConnectionClosedException, DatabaseException}
import com.hivemind.app.database.model.{TableName, *}
import com.hivemind.app.logging.Logger
import zio.*
import zio.test.*
import zio.test.Assertion.*

import java.io.IOException
import scala.reflect.ClassTag

object DatabaseSpec extends ZIOSpecDefault {

  val helloWorldTest: Spec[Any, IOException] = test("sayHello correctly displays output") {
    for {
      _      <- Console.printLine("Hello, World!")
      output <- TestConsole.output
    } yield assertTrue(output == Vector("Hello, World!\n"))
  }

  val test1: Spec[Any, DatabaseException] = test("returns data when getObjectById(1, TableName.Users) is executed") {
    val fixture = new TestConfiguration

    for {
      db     <- fixture.databaseIO
      record <- db.getObjectById(1, TableName.Users)
    } yield assertAlonzoChurch(record)
  }

  val test2: Spec[Any, Option[Record]] = test("returns an exception when getObjectById is executed") {
    val fixture2 = new TestConfiguration {
      override lazy val probability: Double = 100.0
    }

    for {
      db    <- fixture2.databaseIO
      error <- db.getObjectById(1, TableName.Users).flip
    } yield assert(error)(isSubtype[DatabaseConnectionClosedException](anything))
  }

  def spec: Spec[TestEnvironment with Scope, Any] = suite("Database implementation")(helloWorldTest, test1, test2)

  private def assertAlonzoChurch(value: Option[Record]): TestResult =
    assert(value)(isSome(equalTo(UserRecord(id = 1, name = "Alonzo", surname = "Church", age = 33))))
}

class TestConfiguration {
  lazy val probability: Double                        = neverFails
  lazy val testConfig: Config                         = Config(DatabaseParameters("myDB", "password", 5, probabilityOfError = probability))
  val neverFails: Double                              = 0.0
  val alwaysFail: Double                              = 100.0
  val testConfigZLayer: ULayer[Config]                = ZLayer.succeed(testConfig)
  val consoleZLayer: ULayer[Console.ConsoleLive.type] = ZLayer.succeed(zio.Console.ConsoleLive)
  val dbLive: ZIO[Database, Nothing, Database]        = for {
    db <- ZIO.service[Database]
  } yield db

  val databaseIO: UIO[Database] = dbLive.provide(consoleZLayer, testConfigZLayer, Logger.live, Database.live)
}
