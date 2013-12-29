package util

import scalaz.{Failure, Success, Validation}
import org.specs2.execute.FailureException

object ScalazValidationTestUtils {
  implicit class ValidationWithTestUtils[+E, +A](v: Validation[E, A]) {
    def get: A = v match {
      case Success(value) => value
      case Failure(f) => throw new FailureException(org.specs2.execute.Failure("Validation failed with " +f, "success"))
    }
  }
}
