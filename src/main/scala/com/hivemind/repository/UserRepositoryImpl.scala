package com.hivemind.repository

import com.hivemind.database.Database
import com.hivemind.database.exception.{DatabaseConnectionError, DatabaseException, DatabaseTimeoutException}
import com.hivemind.database.model.{Record, TableName, UserRecord}
import com.hivemind.model.User
import com.hivemind.repository.exception.{RepositoryConnectionError, RepositoryException}
import zio.{IO, ZIO}

import java.util.UUID

class UserRepositoryImpl(database: Database) extends UserRepository {
  override def getUserById(id: UUID): IO[RepositoryException, Option[User]] =
    for {
      maybeRecord <- database.getObjectById(id, TableName.Users).mapError(mapDatabaseToRepositoryError)
      maybeUser   <- ZIO.succeed(maybeRecord.flatMap(createUser))
    } yield maybeUser

  private def mapDatabaseToRepositoryError(error: DatabaseException): RepositoryException = error match {
    case DatabaseTimeoutException =>
      RepositoryConnectionError("Timeout waiting for database")
    case DatabaseConnectionError  =>
      RepositoryConnectionError("Database connection exception")
  }

  private def createUser(record: Record): Option[User] = record match {
    case userRecord: UserRecord =>
      Some(User(id = userRecord.id, name = userRecord.name, surname = userRecord.surname, age = userRecord.age))
    case _                      =>
      Option.empty[User]
  }
}
