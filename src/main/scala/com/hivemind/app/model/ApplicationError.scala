package com.hivemind.app.model

import zio.UIO

trait ApplicationError {
  def logError(): UIO[String]
}
