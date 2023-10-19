package com.hivemind.repository

import com.hivemind.database.Database
import com.hivemind.database.exception.{DatabaseConnectionClosedException, DatabaseConnectionException, DatabaseTimeoutException}
import com.hivemind.database.model.{Record, TableName, UserRecord}
import com.hivemind.model.User
import com.hivemind.repository.exception.{RepositoryConnectionError, RepositoryException}
import zio.{IO, ZIO}

class UserRepositoryImpl(database: Database) extends UserRepository {
  override def getUserById(id: Int): IO[RepositoryException, Option[User]] =
    for {
      maybeRecord <- database.getObjectById(id, TableName.Users).mapError {
                       case DatabaseTimeoutException    =>
                         RepositoryConnectionError
                       case DatabaseConnectionException =>
                         RepositoryConnectionError
                       case DatabaseConnectionClosedException   =>
                         RepositoryConnectionError
                     }
      maybeUser   <- ZIO.succeed(maybeRecord.flatMap(buildUserFromRecord))
    } yield maybeUser

  private def buildUserFromRecord(record: Record): Option[User] = record match {
    case userRecord: UserRecord =>
      Some(User(id = userRecord.id, name = userRecord.name, surname = userRecord.surname, age = userRecord.age))
    case _                      =>
      Option.empty[User]
  }
}
