package security

import Permissions._
import Entities._
import Authorities._
import models.ArticleModels.ArticleListModel
import models.ArticleModels.ArticleDetailsModel
import models.CommentModels.Comment
import scalaz._
import Scalaz._

object Permissions {
  sealed trait Permission
  case object Create extends Permission
  case object Read extends Permission
  case object Update extends Permission
  case object Delete extends Permission
  case object Manage extends Permission // ALL
}

object Entities {
  sealed trait Entity
  case object Article extends Entity
  case object Comment extends Entity
}

object Authorities {
  sealed trait Authority
  case object Admin extends Authority
  case object User extends Authority
  case object Anonymous extends Authority
}

case class Principal(id: Option[Int], authority: Authority) {

  if (authority != Anonymous && !id.isDefined) {
    throw new RuntimeException("Authenticated user doesn't have identity")
  }

  // check permissions for "collection of objects" or "objects in general"
  def can(permission: Permission, entity: Entity): Boolean = {
    (authority, permission) match {
      // admin can do everything with anything
      case (Admin, _) => true

      // user can only read and create objects
      case (User, Read | Create) => true
      case (User, _) => false

      // anonymous have read-only rights
      case (Anonymous, Read) => true
      case (Anonymous, _) => false

      case t => throw new RuntimeException(s"Authorization not configured for $t")
    }
  }

  // check permissions for specific object
  def can(permission: Permission, obj: AnyRef): Boolean = {
    (authority, permission, obj) match {
      // admin can do everything with anything
      case (Admin, _, _) => true

      // user can do anything with comments and articles written by him
      // and read any others
      case (User, Read, _) => true
      case (User, _, article: ArticleListModel) => article.author.id == id.get
      case (User, _, article: ArticleDetailsModel) => article.author.id == id.get
      case (User, _, comment: Comment) => comment.author.id == id.get

      // anonymous have read-only rights on all objects
      case (Anonymous, Read, _) => true
      case (Anonymous, _, _) => false

      case t => throw new RuntimeException(s"Authorization not configured for $t")
    }
  }
}
