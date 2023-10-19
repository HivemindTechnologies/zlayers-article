package com.hivemind.app.logging

enum HivemindLogLevel(value: String) {

  def getPrefix: String = value

  case INFO  extends HivemindLogLevel("INFO")
  case DEBUG extends HivemindLogLevel("DEBUG")
  case ERROR extends HivemindLogLevel("ERROR")
}
