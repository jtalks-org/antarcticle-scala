package org.jtalks.antarcticle.persistence

import org.jtalks.antarcticle.persistence.schema.{UsersComponent, ArticlesComponent}


trait Schema extends UsersComponent with ArticlesComponent { self: Profile =>
  def schema = (Articles.ddl ++ Users.ddl)
}