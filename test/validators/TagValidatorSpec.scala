package validators

import org.specs2.mutable.Specification
import scalaz._
import Scalaz._
import org.specs2.scalaz.ValidationMatchers
import models.ArticleModels.Article

class TagValidatorSpec extends Specification with ValidationMatchers {
  val validator = new TagValidator

  "tag validation" should {
    "be successful with valid tags" in {
      val validTags = Vector("abc", "a Bc", "ывВфыв", ".net", "node.js",
               "i`t's", "qWe?", "a-n-t", "C#", "c++", "a"*30)

      val validationResults = validTags.map(validator.validate)

      forall(validationResults)(_ must beSuccessful)
    }

    "fail with invalid tags" in {
      val invalidTags = Vector(".", "/", "A/bc", "<html>", "{}", "..", "a"*31)

      val validationResults = invalidTags.map(validator.validate)

      forall(validationResults)(_ must beFailing)
    }
  }
}


