package com.hivemind.app.model

import com.hivemind.app.logging.Logger
import zio.UIO

trait ApplicationError extends Exception {
  def logError(logger: Logger): UIO[String]
}
