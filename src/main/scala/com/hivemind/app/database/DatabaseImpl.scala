package com.hivemind.app.database

import com.hivemind.app.database.Database
import com.hivemind.app.database.DatabaseImpl.{propertiesById, usersById}
import com.hivemind.app.database.exception.{DatabaseConnectionClosedException, DatabaseException, DatabaseQueryExecutionException, DatabaseTimeoutException}
import com.hivemind.app.database.model.{DatabaseParameters, TableName, *}
import com.hivemind.app.logging.Logger
import zio.*

import scala.collection.immutable.HashMap
import scala.util.Random

class DatabaseImpl(parameters: DatabaseParameters, logger: Logger) extends Database {

  private val maxQueryDuration: Int      = 10
  private val probabilityOfError: Double = 5 / 100

  override def getObjectById(id: Int, table: TableName): IO[DatabaseException, Option[Record]] = {

    val connection = DatabaseConnection(isAlive = Random.nextBoolean)

    val result: IO[DatabaseException, Option[Record]] = for {
      _           <- checkConnectionAlive(connection)
      _           <- simulateExecutionTime(parameters.maxSecondsForQuery)
      maybeRecord <- simulateRetrieveResults(getRecordById(id, table))
    } yield maybeRecord

    handleDBErrors(result)
  }

  override def simulateQuery(table: TableName): IO[DatabaseException, List[Record]] = {
    val connection                                  = DatabaseConnection(isAlive = generateBooleanWithProvidedProbability)
    val result: IO[DatabaseException, List[Record]] = for {
      _       <- checkConnectionAlive(connection)
      _       <- simulateExecutionTime(parameters.maxSecondsForQuery)
      records <- simulateRetrieveResults(generateRandomListOfResults(table))
    } yield records

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
      maybeUnit <- ZIO
                     .sleep((Random.nextInt(maxQueryDuration) + 1).seconds)
                     .timeout(timeout.seconds)
      outcome   <- if maybeUnit.isDefined
                   then ZIO.unit
                   else ZIO.fail(DatabaseTimeoutException(logger))
    } yield outcome

    result.catchAll((dbException: DatabaseException) => dbException.logError() *> result)
  }

  private def generateRandomListOfResults(table: TableName): List[Record] = {
    val maxRecordsToReturn: Int = 7
    val maxIdInclusive: Int     = 7

    val recordsToReturn: Int = Random.nextInt(maxRecordsToReturn) + 1

    val results: Set[Record] = (1 to recordsToReturn).toSet.flatMap { _ =>

      val randomId = Random.nextInt(maxIdInclusive) + 1
      getRecordById(randomId, table).toSet
    }

    results.toList
  }

  private def getRecordById(id: Int, table: TableName): Option[Record] =
    table match {
      case TableName.Users      =>
        usersById.get(id)
      case TableName.Properties =>
        propertiesById.get(id)
    }

  private def simulateRetrieveResults[A, M[_]](result: M[A]): IO[DatabaseException, M[A]] = {

    val outcome: IO[DatabaseException, M[A]] =
      if generateBooleanWithProvidedProbability
      then ZIO.succeed(result)
      else
        ZIO
          .fail(DatabaseQueryExecutionException(logger))

    outcome
  }

  private def generateBooleanWithProvidedProbability: Boolean =
    Random.between(0.0, 100.0) > probabilityOfError
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
