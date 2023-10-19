package com.hivemind.model

import zio.UIO

trait ApplicationError {
  def logError(): UIO[String]
}
