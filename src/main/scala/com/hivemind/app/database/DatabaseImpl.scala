package com.hivemind.app.database

import com.hivemind.app.config.DatabaseParameters
import com.hivemind.app.database.DatabaseImpl.{propertiesById, usersById}
import com.hivemind.app.database.exception.{DatabaseConnectionClosedException, DatabaseException, DatabaseQueryExecutionException, DatabaseTimeoutException}
import com.hivemind.app.database.model.{TableName, *}
import com.hivemind.app.logging.Logger
import zio.*
import zio.Random.nextDoubleBetween

import scala.collection.immutable.HashMap
import scala.util.Random.nextInt as scalaNextInt

class DatabaseImpl(parameters: DatabaseParameters, logger: Logger) extends Database {

  private val maxQueryDuration: Int      = 10
  private val probabilityOfError: Double = parameters.probabilityOfError

  override def getObjectById(id: Int, table: TableName): IO[DatabaseException, Option[Record]] = {

    val result: IO[DatabaseException, Option[Record]] = for {
      connectionClosedError <- isErrorUsingProvidedProbability
      connection             = DatabaseConnection(isAlive = !connectionClosedError)
      _                     <- checkConnectionAlive(connection)
      _                     <- simulateExecutionTime(parameters.queryTimeoutSeconds)
      maybeRecord           <- simulateRetrieveResults(getRecordById(id, table))
    } yield maybeRecord

    handleDBErrors(result)
  }

  private def handleDBErrors[A](effect: IO[DatabaseException, A]): IO[DatabaseException, A] =
    effect.catchAll((dbException: DatabaseException) => dbException.logError() *> effect)

  private def checkConnectionAlive(connection: DatabaseConnection): IO[DatabaseException, Unit] =
    if connection.isAlive
    then ZIO.unit
    else ZIO.fail(DatabaseConnectionClosedException(logger))

  private def simulateExecutionTime(timeout: Int): IO[DatabaseException, Unit] = {
    val result = for {
      randomTime <- Random.nextIntBetween(1, maxQueryDuration + 1)
      maybeUnit  <- ZIO
                      .sleep(randomTime.seconds)
                      .timeout(timeout.seconds)
      outcome    <- if maybeUnit.isDefined
                    then ZIO.unit
                    else ZIO.fail(DatabaseTimeoutException(logger))
    } yield outcome

    result.catchAll((dbException: DatabaseException) => dbException.logError() *> result)
  }

  private def getRecordById(id: Int, table: TableName): Option[Record] =
    table match {
      case TableName.Users      =>
        usersById.get(id)
      case TableName.Properties =>
        propertiesById.get(id)
    }

  private def simulateRetrieveResults[A, M[_]](result: M[A]): IO[DatabaseException, M[A]] =
    for {
      isError <- isErrorUsingProvidedProbability
      outcome <- if isError
                 then ZIO.fail(DatabaseQueryExecutionException(logger))
                 else ZIO.succeed(result)
    } yield outcome

  private def isErrorUsingProvidedProbability: UIO[Boolean] =
    for {
      double  <- Random.nextDoubleBetween(0.0, 100.0)
      isError <- ZIO.succeed(double < probabilityOfError)
    } yield isError

  override def simulateQuery(table: TableName): IO[DatabaseException, List[Record]] = {
    val result: IO[DatabaseException, List[Record]] = for {
      isConnectionClosedError <- isErrorUsingProvidedProbability
      connection               = DatabaseConnection(isAlive = !isConnectionClosedError)
      _                       <- checkConnectionAlive(connection)
      _                       <- simulateExecutionTime(parameters.queryTimeoutSeconds)
      records                 <- simulateRetrieveResults(generateRandomListOfResults(table))
    } yield records

    handleDBErrors(result)
  }

  private def generateRandomListOfResults(table: TableName): List[Record] = {
    val maxRecordsToReturn: Int = 7
    val maxIdInclusive: Int     = 7

    val recordsToReturn: Int = scalaNextInt(maxRecordsToReturn) + 1

    val results: Set[Record] = (1 to recordsToReturn).toSet.flatMap { _ =>

      val randomId = scalaNextInt(maxIdInclusive) + 1
      getRecordById(randomId, table).toSet
    }

    results.toList
  }
}

object DatabaseImpl {

  private type DatabaseTable = Map[Int, Record]

  val usersById: DatabaseTable =
    HashMap(
      1 -> UserRecord(id = 1, name = "Alonzo", surname = "Church", age = 33),
      2 -> UserRecord(id = 2, name = "Alan", surname = "Turing", age = 31),
      3 -> UserRecord(id = 3, name = "Haskell", surname = "Curry", age = 46),
    )

  val propertiesById: DatabaseTable =
    HashMap(
      1 -> PropertyRecord(id = 1, propertyType = "Car", price = 17800, owner = 1),
      2 -> PropertyRecord(id = 2, propertyType = "House", price = 230500, owner = 1),
      3 -> PropertyRecord(id = 3, propertyType = "Boat", price = 180000, owner = 1),
      4 -> PropertyRecord(id = 4, propertyType = "House", price = 117900, owner = 2),
      5 -> PropertyRecord(id = 5, propertyType = "Car", price = 2000, owner = 2),
      6 -> PropertyRecord(id = 6, propertyType = "House", price = 124900, owner = 3),
      7 -> PropertyRecord(id = 7, propertyType = "Car", price = 6500, owner = 3),
    )

}
