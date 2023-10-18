package com.hivemind.database.model

import com.hivemind.model.PropertyType
import java.util.UUID

sealed trait Record

case class UserRecord(id: UUID, name: String, surname: String, age: Int)    extends Record
case class PropertyRecord(id: UUID, propertyType: PropertyType, price: Int) extends Record
