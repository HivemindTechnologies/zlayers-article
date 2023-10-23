package com.hivemind.app.repository.user

import com.hivemind.app.database.Database
import com.hivemind.app.database.exception.{DatabaseConnectionClosedException, DatabaseQueryExecutionException, DatabaseTimeoutException}
import com.hivemind.app.database.model.{Record, TableName, UserRecord}
import com.hivemind.app.model.User
import com.hivemind.app.repository.exception.{RepositoryConnectionError, RepositoryException}
import zio.{IO, ZIO}

class UserRepositoryImpl(database: Database) extends UserRepository {

  import UserRepositoryImpl.*

  override def getUserById(id: Int): IO[RepositoryException, Option[User]] =
    for {
      maybeRecord <- database.getObjectById(id, TableName.Users).mapError {
                       case DatabaseTimeoutException(logger)          =>
                         RepositoryConnectionError
                       case DatabaseQueryExecutionException(logger)   =>
                         RepositoryConnectionError
                       case DatabaseConnectionClosedException(logger) =>
                         RepositoryConnectionError
                     }
      maybeUser   <- ZIO.succeed(maybeRecord.flatMap(buildUserFromRecord))
    } yield maybeUser

}

object UserRepositoryImpl {
  def buildUserFromRecord(record: Record): Option[User] = record match {
    case userRecord: UserRecord =>
      Some(User(id = userRecord.id, name = userRecord.name, surname = userRecord.surname, age = userRecord.age))
    case _                      =>
      Option.empty[User]
  }
}
