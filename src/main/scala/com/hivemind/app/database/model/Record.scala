package com.hivemind.app.database.model

sealed trait Record {
  def toUserRecord: Option[UserRecord]
  def toPropertyRecord: Option[PropertyRecord]
}

type UserId     = Int
type PropertyId = Int

case class UserRecord(id: UserId, name: String, surname: String, age: Int) extends Record {
  override def toUserRecord: Option[UserRecord]         = Some(this)
  override def toPropertyRecord: Option[PropertyRecord] = None
}

case class PropertyRecord(id: PropertyId, propertyType: String, price: Int, owner: UserId) extends Record {
  override def toUserRecord: Option[UserRecord]         = None
  override def toPropertyRecord: Option[PropertyRecord] = Some(this)
}
