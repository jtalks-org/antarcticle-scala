package validators

import models.ArticleModels.Language._
import org.specs2.mutable.Specification
import scalaz._
import Scalaz._
import org.specs2.scalaz.ValidationMatchers
import models.ArticleModels.Article

class ArticleValidatorSpec extends Specification with ValidationMatchers {
  val validator = new ArticleValidator(new TagValidator)

  "article validation" should {
    val article = Article(None, "dfds", "", List("tag1", "tag 2"), Russian, None)

    "fail when title is too long" in {
      validator.validate(article.copy(title = "a" * 61)) must beFailing
    }

    "fail when title is empty" in {
      validator.validate(article.copy(title = "")) must beFailing
    }

    "fail when title contains only spaces" in {
      validator.validate(article.copy(title = "   ")) must beFailing
    }

    "fail when content is too long" in {
      validator.validate(article.copy(content = "d" * 65001)) must beFailing
    }

    "fail when there are more then 10 tags" in {
      val tags = (1 to 11).map(n => s"tag$n")

      validator.validate(article.copy(tags = tags)) must beFailing
    }

    "ignore duplicates when counting tags" in {
      val tags = (1 to 11).map(n => "tag")

      validator.validate(article.copy(tags = tags)) must beSuccessful
    }

    "fail with multiple failures" in {
       validator.validate(article.copy(title = "", content = "d" * 65001)) must beFailing.like {
        case nel => nel.size must_== 2
       }
    }

    "be successful when article is valid" in {
      validator.validate(article) must beSuccessful
    }
  }
}
