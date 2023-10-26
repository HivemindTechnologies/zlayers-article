package com.hivemind.app.database.exception

enum DatabaseLayerExecutionOutcome {
  case FinishWithoutErrors, RaiseConnectionClosedError, RaiseTimeoutError, RaiseQueryExecutionError
}
