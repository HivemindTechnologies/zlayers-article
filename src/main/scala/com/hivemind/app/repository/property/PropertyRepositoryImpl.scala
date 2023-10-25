package com.hivemind.app.repository.property

import com.hivemind.app.database.Database
import com.hivemind.app.database.model.{PropertyRecord, Record, TableName, UserRecord}
import com.hivemind.app.logging.Logger
import com.hivemind.app.model.{Property, PropertyType}
import com.hivemind.app.repository.exception.RepositoryException
import com.hivemind.app.repository.exception.RepositoryException.handleDatabaseErrors
import com.hivemind.app.repository.property.PropertyRepositoryImpl.buildPropertyFromRecord
import com.hivemind.app.repository.user.UserRepositoryImpl.*
import zio.{IO, ZIO}

class PropertyRepositoryImpl(database: Database, logger: Logger) extends PropertyRepository {

  override def getPropertyById(id: Int): IO[RepositoryException, Option[Property]] =
    for {
      maybeRecord        <- handleDatabaseErrors(database.getObjectById(id, TableName.Properties), logger)
      maybePropertyRecord = maybeRecord.fold(Option.empty[PropertyRecord])((record: Record) => record.toPropertyRecord)
      maybeUserRecord    <- getUserOfProperty(maybePropertyRecord)
    } yield buildPropertyFromRecord(maybePropertyRecord, maybeUserRecord)

  private def getUserOfProperty(maybePropertyRecord: Option[PropertyRecord]): IO[RepositoryException, Option[UserRecord]] = {
    val emptyUserRecordZIO = ZIO.succeed(Option.empty[UserRecord])

    maybePropertyRecord.fold(emptyUserRecordZIO) { (propertyRecord: PropertyRecord) =>
      for {
        maybeRecord     <- handleDatabaseErrors(database.getObjectById(propertyRecord.owner, TableName.Users), logger)
        maybeUserRecord <- maybeRecord.fold(emptyUserRecordZIO)((record: Record) => ZIO.succeed(record.toUserRecord))
        userRecord      <- maybeUserRecord.fold(emptyUserRecordZIO)((userRecord: UserRecord) => ZIO.succeed(userRecord))
      } yield maybeUserRecord
    }
  }

  override def getPropertiesByOwnerId(userId: Int): IO[RepositoryException, List[Property]] =
    for {
      maybeRecord            <- handleDatabaseErrors(database.getObjectById(userId, TableName.Users), logger)
      maybeUserRecord         = maybeRecord.flatMap((record: Record) => record.toUserRecord)
      allRecords             <- handleDatabaseErrors(database.getAllRecords(TableName.Properties), logger)
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
