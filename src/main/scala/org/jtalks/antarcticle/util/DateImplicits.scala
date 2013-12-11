package org.jtalks.antarcticle.util

import java.sql.Timestamp
import org.joda.time.DateTime

object DateImplicits {
  implicit def timestampToDateTime(ts: Timestamp): DateTime = new DateTime(ts.getTime)
  implicit def dateTimeToTimestamp(dt: DateTime): Timestamp = new Timestamp(dt.getMillis)
}
