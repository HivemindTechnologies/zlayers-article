package com.hivemind.app.repository.property

import com.hivemind.app.database.Database
import com.hivemind.app.database.exception.{DatabaseConnectionClosedException, DatabaseException, DatabaseQueryExecutionException, DatabaseTimeoutException}
import com.hivemind.app.database.model.{PropertyRecord, Record, TableName, UserRecord}
import com.hivemind.app.model.{Property, PropertyType}
import com.hivemind.app.repository.exception.{RepositoryConnectionError, RepositoryException}
import com.hivemind.app.repository.property.PropertyRepositoryImpl.buildPropertyFromRecord
import com.hivemind.app.repository.user.UserRepositoryImpl.*
import zio.{IO, ZIO}

class PropertyRepositoryImpl(database: Database) extends PropertyRepository {

  override def getPropertyById(id: Int): IO[RepositoryException, Option[Property]] =
    for {
      maybeRecord        <- database.getObjectById(id, TableName.Properties).mapError(mapErrors)
      maybePropertyRecord = maybeRecord.fold(Option.empty[PropertyRecord])((record: Record) => record.toPropertyRecord)
      maybeUserRecord    <- getUserOfProperty(maybePropertyRecord)
    } yield buildPropertyFromRecord(maybePropertyRecord, maybeUserRecord)

  private def getUserOfProperty(maybePropertyRecord: Option[PropertyRecord]): IO[RepositoryException, Option[UserRecord]] = {
    val emptyUserRecordZIO = ZIO.succeed(Option.empty[UserRecord])

    maybePropertyRecord.fold(emptyUserRecordZIO) { (propertyRecord: PropertyRecord) =>
      for {
        maybeRecord     <- database.getObjectById(propertyRecord.owner, TableName.Users).mapError(mapErrors)
        maybeUserRecord <- maybeRecord.fold(emptyUserRecordZIO)((record: Record) => ZIO.succeed(record.toUserRecord))
        userRecord      <- maybeUserRecord.fold(emptyUserRecordZIO)((userRecord: UserRecord) => ZIO.succeed(userRecord))
      } yield maybeUserRecord
    }
  }

  private def mapErrors(error: DatabaseException): RepositoryException = error match {
    case DatabaseTimeoutException(logger)          =>
      RepositoryConnectionError
    case DatabaseQueryExecutionException(logger)   =>
      RepositoryConnectionError
    case DatabaseConnectionClosedException(logger) =>
      RepositoryConnectionError
  }

  override def getPropertyByOwnerId(userId: Int): IO[RepositoryException, List[Property]] =
    for {
      maybeRecord            <- database.getObjectById(userId, TableName.Users).mapError(mapErrors)
      maybeUserRecord         = maybeRecord.flatMap((record: Record) => record.toUserRecord)
      allRecords             <- database.getAllRecords(TableName.Properties).mapError(mapErrors)
      propertyRecordsFiltered = allRecords.filter { (record: Record) =>
                                  val maybePropertyRecord = record.toPropertyRecord
                                  maybePropertyRecord.fold(false)((propertyRecord: PropertyRecord) => propertyRecord.owner == userId)
                                }
      propertyRecords         = propertyRecordsFiltered.flatMap(_.toPropertyRecord.toList)
      listOfProperties        = propertyRecords.flatMap((propertyRecord: PropertyRecord) => buildPropertyFromRecord(Some(propertyRecord), maybeUserRecord))
    } yield listOfProperties
}

object PropertyRepositoryImpl {
  def buildPropertyFromRecord(maybePropertyRecord: Option[PropertyRecord], maybeUserRecord: Option[UserRecord]): Option[Property] =
    for {
      propertyRecord <- maybePropertyRecord
      propertyType   <- propertyTypeOfString(propertyRecord.propertyType)
      userRecord     <- maybeUserRecord
      user           <- buildUserFromRecord(userRecord)
    } yield Property(id = propertyRecord.id, kind = propertyType, price = propertyRecord.price, owner = user)

  private def propertyTypeOfString(value: String): Option[PropertyType] =
    value.toLowerCase() match {
      case "car"   =>
        Some(PropertyType.Car)
      case "house" =>
        Some(PropertyType.House)
      case "boat"  =>
        Some(PropertyType.Boat)
      case _       =>
        Option.empty[PropertyType]
    }
}
