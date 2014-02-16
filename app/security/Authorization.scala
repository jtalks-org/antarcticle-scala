package security

import Permissions._
import Entities._
import Authorities._
import models.ArticleModels.ArticleListModel
import models.ArticleModels.ArticleDetailsModel
import models.CommentModels.Comment
import scalaz._
import Scalaz._
import models.database.ArticleRecord

object Permissions {
  sealed trait Permission
  case object Create extends Permission
  case object Read extends Permission
  case object Update extends Permission
  case object Delete extends Permission
  case object Manage extends Permission // ALL
}

// class of objects (collection of objects)
object Entities {
  sealed trait Entity
  case object Article extends Entity
  case object Comment extends Entity
}

// user "roles"
object Authorities {
  sealed trait Authority
  case object Admin extends Authority
  case object User extends Authority
}

trait Principal {
  // check permissions for "collection of objects" or "objects in general"
  def can(permission: Permission, entity: Entity): Boolean
  // check permissions for specific object
  def can(permission: Permission, obj: AnyRef): Boolean

  def isAuthenticated: Boolean
}

// principal for not authenticated users
case object AnonymousPrincipal extends Principal {
  override def can(permission: Permission, entity: Entity): Boolean = {
    permission match {
      // anonymous have read-only rights
      case Read => true
      case _ => false
    }
  }

  override def can(permission: Permission, obj: AnyRef): Boolean = {
    permission match {
      // anonymous have read-only rights on all objects
      case Read => true
      case _ => false
    }
  }

  override def isAuthenticated = false
}

// principal for authenticated user
class AuthenticatedPrincipal(sid: Int, authority: Authority) extends Principal {
  override def can(permission: Permission, entity: Entity): Boolean = {
    (authority, permission) match {
      // admin can do everything with anything
      case (Admin, _) => true

      // user can only read and create objects
      case (User, Read | Create) => true
      case (User, _) => false

      case t => notConfigured(t)
    }
  }

  override def can(permission: Permission, obj: AnyRef): Boolean = {
    (authority, permission, obj) match {
      // admin can do everything with anything
      case (Admin, _, _) => true

      // user can do anything with comments and articles written by him
      // and read any other objects
      case (User, Read, _) => true
      case (User, _, article: ArticleListModel) => article.author.id == sid
      case (User, _, article: ArticleDetailsModel) => article.author.id == sid
      case (User, _, article: ArticleRecord) => article.authorId == sid
      case (User, _, comment: Comment) => comment.author.id == sid

      case t => notConfigured(t)
    }
  }

  override def isAuthenticated = true

  private def notConfigured(t: Any) =
    throw new RuntimeException(s"Authenticated principal authorization is not configured for $t")
}
