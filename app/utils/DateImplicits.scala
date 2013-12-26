package utils

import java.sql.Timestamp
import org.joda.time.DateTime

/**
 * Impicit conversions between date and time formats
 */
object DateImplicits {
  // convert java.sql.Timestamp to joda
  implicit def timestampToDateTime(ts: Timestamp): DateTime = new DateTime(ts.getTime)
  // convert joda to java.sql.Timestamp
  implicit def dateTimeToTimestamp(dt: DateTime): Timestamp = new Timestamp(dt.getMillis)
}
