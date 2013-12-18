package utils

import java.sql.Timestamp
import org.joda.time.DateTime

/**
 * Impicit conversions between date and time formats
 */
object DateImplicits {
  implicit def timestampToDateTime(ts: Timestamp): DateTime = new DateTime(ts.getTime)
  implicit def dateTimeToTimestamp(dt: DateTime): Timestamp = new Timestamp(dt.getMillis)
}
