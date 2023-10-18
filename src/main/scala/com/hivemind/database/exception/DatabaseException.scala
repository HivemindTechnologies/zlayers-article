package com.hivemind.database.exception

sealed trait DatabaseException

case object TimeoutException    extends DatabaseException
case object ConnectionException extends DatabaseException
