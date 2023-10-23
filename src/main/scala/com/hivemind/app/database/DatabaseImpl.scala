package com.hivemind.app.database

import com.hivemind.app.config.DatabaseParameters
import com.hivemind.app.database.DatabaseImpl.{propertiesById, usersById}
import com.hivemind.app.database.exception.{DatabaseConnectionClosedException, DatabaseException, DatabaseQueryExecutionException, DatabaseTimeoutException}
import com.hivemind.app.database.model.{TableName, *}
import com.hivemind.app.logging.Logger
import zio.*

import scala.collection.immutable.HashMap
import scala.util.Random.{between as scalaNextDouble, nextInt as scalaNextInt}

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
    else ZIO.fail(DatabaseConnectionClosedException(logger))

  private def simulateExecutionTime: IO[DatabaseException, Unit] = {
    val result = for {
      shouldFail <- randomErrorUsingGivenProbability
      outcome    <- if shouldFail
                    then ZIO.fail(DatabaseTimeoutException(logger))
                    else ZIO.unit
    } yield outcome

    result
  }

  private def simulateRetrieveResults[A, M[_]](result: M[A]): IO[DatabaseException, M[A]] =
    for {
      isError <- randomErrorUsingGivenProbability
      outcome <- if isError
                 then ZIO.fail(DatabaseQueryExecutionException(logger))
                 else ZIO.succeed(result)
    } yield outcome

  private def randomErrorUsingGivenProbability: UIO[Boolean] =
    for {
      double  <- ZIO.succeed(scalaNextDouble(0.0, 100.0))
      isError <- ZIO.succeed(double < probabilityOfError)
    } yield isError

  private def getRecordById(id: Int, table: TableName): Option[Record] =
    table match {
      case TableName.Users      =>
        usersById.get(id)
      case TableName.Properties =>
        propertiesById.get(id)
    }

  override def executeQuery(table: TableName): IO[DatabaseException, List[Record]] = {
    val result: IO[DatabaseException, List[Record]] = for {
      isConnectionClosedError <- randomErrorUsingGivenProbability
      connection               = DatabaseConnection(isAlive = !isConnectionClosedError)
      _                       <- checkConnectionAlive(connection)
      _                       <- simulateExecutionTime
      notDeterministic        <- randomErrorUsingGivenProbability
      results                 <- ZIO.succeed(
                                   if notDeterministic
                                   then randomListOfResults(table)
                                   else fixedListOfResults(table),
                                 )
      records                 <- simulateRetrieveResults(results)
    } yield records

    result
  }

  private def randomListOfResults(table: TableName): List[Record] = {
    val maxRecordsToReturn: Int = 7
    val maxIdInclusive: Int     = 7

    val recordsToReturn: Int = scalaNextInt(maxRecordsToReturn) + 1

    val results: Set[Record] = (1 to recordsToReturn).toSet.flatMap { _ =>

      val randomId = scalaNextInt(maxIdInclusive) + 1
      getRecordById(randomId, table).toSet
    }

    results.toList
  }

  private def fixedListOfResults(table: TableName): List[Record] =
    (1 to 3).toList.flatMap { index =>
      getRecordById(index, table).toList
    }
}

object DatabaseImpl {
  private type DatabaseTable = Map[Int, Record]

  val alonzoChurch: UserRecord = UserRecord(id = 1, name = "Alonzo", surname = "Church", age = 33)
  val alanTuring: UserRecord   = UserRecord(id = 1, name = "Alonzo", surname = "Church", age = 33)
  val haskellCurry: UserRecord = UserRecord(id = 3, name = "Haskell", surname = "Curry", age = 46)

  val usersById: DatabaseTable =
    HashMap(
      1 -> alonzoChurch,
      2 -> alanTuring,
      3 -> haskellCurry,
    )

  val car1   = PropertyRecord(id = 1, propertyType = "Car", price = 17800, owner = 1)
  val house1 = PropertyRecord(id = 2, propertyType = "House", price = 230500, owner = 1)
  val boat1  = PropertyRecord(id = 3, propertyType = "Boat", price = 180000, owner = 1)
  val house2 = PropertyRecord(id = 4, propertyType = "House", price = 117900, owner = 2)
  val car2   = PropertyRecord(id = 5, propertyType = "Car", price = 2000, owner = 2)
  val house3 = PropertyRecord(id = 6, propertyType = "House", price = 124900, owner = 3)
  val car3   = PropertyRecord(id = 7, propertyType = "Car", price = 6500, owner = 3)

  val propertiesById: DatabaseTable =
    HashMap(
      1 -> car1,
      2 -> house1,
      3 -> boat1,
      4 -> house2,
      5 -> car2,
      6 -> house3,
      7 -> car3,
    )

}
