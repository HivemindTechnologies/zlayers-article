package com.hivemind.app.model

import zio.UIO

trait ApplicationError extends Exception {
  def logError(): UIO[String]
}
