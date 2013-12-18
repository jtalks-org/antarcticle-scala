package util

import org.joda.time.{DateTimeUtils, DateTime}

object TimeFridge {
  /**
   * Call function with frozen time. After function execution sets time back to now.
   * Useful for testing temporary data.
   * WARNING: may cause problem in multi-threaded environment
   */
  def withFrozenTime[T](frozenDateTime: DateTime = DateTime.now)(f: (DateTime) => T) = {
    DateTimeUtils.setCurrentMillisFixed(frozenDateTime.getMillis)
    f(frozenDateTime)
    DateTimeUtils.setCurrentMillisSystem()
  }
}

