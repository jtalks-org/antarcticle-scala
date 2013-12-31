package models

import java.sql.Timestamp
import models.UserModels.UserModel

/**
 *
 */
object CommentModels {
  case class Comment(id: Int, author: UserModel, articleId: Int, content: String,
                     createdAt: Timestamp, updatedAt: Option[Timestamp])
}
