package com.hivemind.app.model

import java.util.UUID

case class Property(id: UUID, kind: PropertyType, price: Int, description: String)
