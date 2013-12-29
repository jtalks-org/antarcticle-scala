package validators

import models.database.Tag
import scalaz._
import Scalaz._

object TagValidator {
  val TAG_MAX_LENGTH = 30
  val TAG_FORMAT = """(^[^\.][\p{L}\d\-#\s\?\+`'\.]+$)|(^\.[\p{L}\d\-#\s\?\+`'][\p{L}\d\-#\s\?\+`'\.]*$)"""
}

class TagValidator extends Validator[String] {
  import TagValidator._

  def validate(tag: String): ValidationNel[String, String] = {
    def checkLength = if (tag.trim.length > TAG_MAX_LENGTH)
                        s"Tag $tag is too long".failNel
                      else
                        tag.successNel

    def checkFormat = if (!tag.matches(TAG_FORMAT))
                        s"Tag $tag should not contain special characters".failNel
                      else
                        tag.successNel

    (checkLength |@| checkFormat) {
      case _ => tag
    }
  }
}
