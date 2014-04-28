package security

import org.specs2.mutable.Specification
import Permissions._
import Entities.Entity
import models.ArticleModels.ArticleListModel
import models.UserModels.UserModel
import models.CommentModels.Comment
import models.ArticleModels.ArticleDetailsModel

// TODO: refactor examples creation in loops
class PrincipalSpec extends Specification {

  // permissions that causes modification
  val modificationPermissions = List(Create, Update, Delete, Manage)
  // all permissions
  val allPermissions = modificationPermissions :+ Read

  // user used as author for objects
  val user = UserModel(1, "")

  // authorization objects
  val articleListModel = ArticleListModel(1, "", "", new java.util.Date(), user, Seq.empty, 1)
  val articleDetailsModel = ArticleDetailsModel(1, "", "", new java.util.Date(), user, Seq.empty)
  val comment = Comment(1, user, 1, "", new java.sql.Timestamp(3423343443L), None)

  val allObjects = List(articleListModel, comment, articleDetailsModel)
  val allEntities = List[Entity](Entities.Article, Entities.Comment)

  def readPermissionsOnAll(principal: Principal) = examplesBlock {
    allEntities.foreach { entity =>
      (s"be able to read $entity collection") >> {
        principal.can(Read, entity) must beTrue
      }
    }

    allObjects.foreach { obj =>
      (s"be able to read ${obj.getClass.getSimpleName} object") >> {
        principal.can(Read, obj) must beTrue
      }
    }
  }

  "anonymous" should {
    "be able to read all objects and collections" >>
      readPermissionsOnAll(AnonymousPrincipal)

    "not be able to modify any object" >> examplesBlock {
      allObjects.foreach { obj =>
        modificationPermissions.foreach { permission =>
          (s"not be able to modify ${obj.getClass.getSimpleName} object") >> {
            AnonymousPrincipal.can(permission, obj) must beFalse
          }
        }
      }
    }

    "not be able to modify any collection" >> examplesBlock {
      allEntities.foreach { entity =>
        modificationPermissions.foreach { permission =>
          (s"not be able to $permission $entity collection") >> {
            AnonymousPrincipal.can(permission, entity) must beFalse
          }
        }
      }
    }
  }


  "user" should {
    val userPrincipal = new AuthenticatedPrincipal(user.id, Authorities.User)

    "be able to read all objects and collections" >>
      readPermissionsOnAll(userPrincipal)

    "be able to create any object" >> examplesBlock {
      allEntities.foreach { entity =>
        (s"be able to create $entity") >> {
          userPrincipal.can(Create, entity) must beTrue
        }
      }
    }

    "be able to modify authored objects" >> examplesBlock {
      allObjects.foreach { obj =>
        modificationPermissions.foreach { permission =>
          (s"be able to $permission $obj") >> {
            userPrincipal.can(permission, obj) must beTrue
          }
        }
      }
    }

    "not be able to modify not authored objects" >> examplesBlock {
       val notAuthorPrincipal = new AuthenticatedPrincipal(99, Authorities.User)
       allObjects.foreach { obj =>
         modificationPermissions.foreach { permission =>
           (s"not be able to $permission $obj") >> {
             notAuthorPrincipal.can(permission, obj) must beFalse
           }
         }
       }
    }
  }

  "admin" should {
    val adminPrincipal = new AuthenticatedPrincipal(9389, Authorities.Admin)

    "be able to anything with any object" >> examplesBlock {
      allObjects.foreach { obj =>
        allPermissions.foreach { permission =>
          (s"be able to $permission $obj") >> {
            adminPrincipal.can(permission, obj) must beTrue
          }
        }
      }
    }

    "be able to anything with any collection" >> examplesBlock {
      allEntities.foreach { entity =>
        allPermissions.foreach { permission =>
          (s"be able to $permission $entity") >> {
            adminPrincipal.can(permission, entity) must beTrue
          }
        }
      }
    }
  }

}
