package com.hivemind.repository

import com.hivemind.database.Database
import com.hivemind.model.User
import com.hivemind.database.model.{Record, TableName, UserRecord}
import com.hivemind.repository.exception.{ConnectionError, RepositoryException}
import zio.IO
import zio.ZIO
import com.hivemind.database.exception.{ConnectionException, DatabaseException, TimeoutException}
import java.util.UUID

class UserRepositoryImpl(database: Database) extends UserRepository {
  override def getUserById(id: UUID): IO[RepositoryException, Option[User]] =
    for {
      maybeRecord <- database.getObjectById(id, TableName.Users).mapError(mapDatabaseToRepositoryError)
      maybeUser   <- ZIO.succeed(maybeRecord.flatMap(createUser))
    } yield maybeUser

  private def mapDatabaseToRepositoryError(error: DatabaseException): RepositoryException = error match {
    case TimeoutException    =>
      ConnectionError("Timeout waiting for database")
    case ConnectionException =>
      ConnectionError("Database connection exception")
  }

  private def createUser(record: Record): Option[User] = record match {
    case userRecord: UserRecord =>
      Some(User(id = userRecord.id, name = userRecord.name, surname = userRecord.surname, age = userRecord.age))
    case _                      =>
      Option.empty[User]
  }
}
