package com.hivemind.app.database

import com.hivemind.app.config.DatabaseParameters
import com.hivemind.app.database.DatabaseImpl.{properties, propertiesById, usersById}
import com.hivemind.app.database.exception.{DatabaseConnectionClosedException, DatabaseException, DatabaseQueryExecutionException, DatabaseTimeoutException}
import com.hivemind.app.database.model.{TableName, *}
import com.hivemind.app.logging.Logger
import zio.*

import scala.collection.immutable.HashMap
import scala.util.Random.between as scalaNextDouble

class DatabaseImpl(parameters: DatabaseParameters, logger: Logger) extends Database {

  private val probabilityOfError: Double = parameters.probabilityOfError

  override def getObjectById(id: Int, table: TableName): IO[DatabaseException, Option[Record]] = {
    val result: IO[DatabaseException, Option[Record]] = for {
      connectionClosedError <- randomErrorUsingGivenProbability
      connection             = DatabaseConnection(isAlive = !connectionClosedError)
      _                     <- checkConnectionAlive(connection)
      _                     <- simulateExecutionTime
      maybeRecord           <- simulateRetrieveResults(getRecordById(id, table))
    } yield maybeRecord

    result
  }

  private def checkConnectionAlive(connection: DatabaseConnection): IO[DatabaseException, Unit] =
    if connection.isAlive
    then ZIO.unit
    else ZIO.fail(DatabaseConnectionClosedException())

  private def simulateExecutionTime: IO[DatabaseException, Unit] = {
    val result = for {
      shouldFail <- randomErrorUsingGivenProbability
      outcome    <- if shouldFail
                    then ZIO.fail(DatabaseTimeoutException())
                    else ZIO.unit
    } yield outcome

    result
  }

  private def randomErrorUsingGivenProbability: UIO[Boolean] =
    for {
      double  <- ZIO.succeed(scalaNextDouble(0.0, 100.0))
      isError <- ZIO.succeed(double < probabilityOfError)
    } yield isError

  private def simulateRetrieveResults[A, M[_]](result: M[A]): IO[DatabaseException, M[A]] =
    for {
      isError <- randomErrorUsingGivenProbability
      outcome <- if isError
                 then ZIO.fail(DatabaseQueryExecutionException())
                 else logger.log("Query finished successfully.") *> ZIO.succeed(result)
    } yield outcome

  private def getRecordById(id: Int, table: TableName): Option[Record] =
    table match {
      case TableName.Users      =>
        usersById.get(id)
      case TableName.Properties =>
        propertiesById.get(id)
    }

  override def getAllRecords(table: TableName): IO[DatabaseException, List[Record]] = {
    val result: IO[DatabaseException, List[Record]] = for {
      isConnectionClosedError <- randomErrorUsingGivenProbability
      connection               = DatabaseConnection(isAlive = !isConnectionClosedError)
      _                       <- checkConnectionAlive(connection)
      _                       <- simulateExecutionTime
      allRecords               = getAllRecordsOfTable(table)
      records                 <- simulateRetrieveResults(allRecords)
    } yield records

    result
  }

  private def getAllRecordsOfTable(table: TableName): List[Record] = {
    val maxRecordId = table match {
      case TableName.Users      =>
        usersById.size
      case TableName.Properties =>
        properties.size
    }
    (1 to maxRecordId).toList.flatMap { index =>
      getRecordById(index, table).toList
    }
  }
}

object DatabaseImpl {
  private lazy val properties: List[PropertyRecord] = List(
    PropertyRecord(id = 1, propertyType = "Car", price = 17800, owner = 1),
    PropertyRecord(id = 2, propertyType = "House", price = 230500, owner = 1),
    PropertyRecord(id = 3, propertyType = "Boat", price = 180000, owner = 1),
    PropertyRecord(id = 4, propertyType = "House", price = 117900, owner = 2),
    PropertyRecord(id = 5, propertyType = "Car", price = 2000, owner = 2),
    PropertyRecord(id = 6, propertyType = "Boat", price = 430000, owner = 2),
    PropertyRecord(id = 7, propertyType = "House", price = 124900, owner = 3),
    PropertyRecord(id = 8, propertyType = "Car", price = 6500, owner = 3),
  )

  val alonzoChurch: UserRecord = UserRecord(id = 1, name = "Alonzo", surname = "Church", age = 33)
  val alanTuring: UserRecord   = UserRecord(id = 2, name = "Alan", surname = "Turing", age = 30)
  val haskellCurry: UserRecord = UserRecord(id = 3, name = "Haskell", surname = "Curry", age = 46)

  val usersById: Map[Int, UserRecord] =
    HashMap(
      1 -> alonzoChurch,
      2 -> alanTuring,
      3 -> haskellCurry,
    )

  val propertiesById: Map[Int, PropertyRecord] =
    properties.zipWithIndex.map((property: PropertyRecord, index: Int) => properties(index).id -> properties(index)).toMap
}
