package com.hivemind.logging

enum MyLogLevel(value: String) {

  def getPrefix: String = value

  case INFO extends MyLogLevel("INFO")
  case DEBUG extends MyLogLevel("DEBUG")
  case ERROR extends MyLogLevel("ERROR")
}
