package validators

import scalaz.ValidationNel

trait Validator[T] {
  def validate(o: T): ValidationNel[String, T]
}
