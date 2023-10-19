package com.hivemind.database.model

sealed trait Record

type UserId     = Int
type PropertyId = Int

case class UserRecord(id: UserId, name: String, surname: String, age: Int)                 extends Record
case class PropertyRecord(id: PropertyId, propertyType: String, price: Int, owner: UserId) extends Record
