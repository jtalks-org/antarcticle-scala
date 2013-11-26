package org.jtalks.antarcticle.persistence

import org.jtalks.antarcticle.persistence.schema.{CommentsComponent, UsersComponent, ArticlesComponent}


trait Schema extends UsersComponent with ArticlesComponent with CommentsComponent { self: Profile =>
  def schema = (Articles.ddl ++ Users.ddl ++ Comments.ddl)
}