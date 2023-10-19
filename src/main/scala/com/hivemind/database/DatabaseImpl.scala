package com.hivemind.database
import com.hivemind.database.DatabaseImpl.{propertiesById, usersById}
import com.hivemind.database.exception.{DatabaseConnectionClosedException, DatabaseConnectionException, DatabaseException, DatabaseTimeoutException}
import com.hivemind.database.model.{DatabaseConnection, DatabaseParameters, PropertyRecord, Record, TableName, UserRecord}
import zio.*

import scala.collection.immutable.HashMap
import scala.util.Random

class DatabaseImpl(parameters: DatabaseParameters) extends Database {

  private val maxQueryDuration: Int = 10

  override def getObjectById(id: Int, table: TableName): IO[DatabaseException, Option[Record]] = {

    val connection = DatabaseConnection(isAlive = Random.nextBoolean)

    for {
      _           <- checkConnectionAlive(connection)
      _           <- simulateExecutionTime(parameters.maxSecondsForQuery)
      maybeRecord <- ZIO
                       .attempt(getRecordById(id, table))
                       .mapError((_: Throwable) => DatabaseConnectionException)
    } yield maybeRecord
  }

  private def checkConnectionAlive(connection: DatabaseConnection): IO[DatabaseException, Unit] =
    if connection.isAlive
    then ZIO.unit
    else ZIO.fail(DatabaseConnectionClosedException)

  private def simulateExecutionTime(timeout: Int): IO[DatabaseException, Unit] =
    for {
      maybeUnit <- ZIO
                     .sleep((Random.nextInt(maxQueryDuration) + 1).seconds)
                     .timeout(timeout.seconds)
      outcome   <- if maybeUnit.isDefined
                   then ZIO.unit
                   else ZIO.fail(DatabaseTimeoutException)
    } yield outcome

  private def getRecordById(id: Int, table: TableName): Option[Record] =
    table match {
      case TableName.Users      =>
        usersById.get(id)
      case TableName.Properties =>
        propertiesById.get(id)
    }

  override def simulateQuery(table: TableName): IO[DatabaseException, List[Record]] = {
    val connection = DatabaseConnection(isAlive = Random.nextBoolean)
    for {
      _       <- checkConnectionAlive(connection)
      _       <- simulateExecutionTime(parameters.maxSecondsForQuery)
      records <- ZIO
                   .attempt(generateRandomListOfResults(table))
                   .mapError((_: Throwable) => DatabaseConnectionException)
    } yield records
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
}

object DatabaseImpl {

  private type DatabaseTable = Map[Int, Record]

  val usersById: DatabaseTable =
    HashMap(
      1 -> UserRecord(id = 1, name = "John", surname = "Baker", age = 33),
      2 -> UserRecord(id = 2, name = "Mary", surname = "Green", age = 31),
      3 -> UserRecord(id = 3, name = "Andrew", surname = "Adams", age = 46),
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
