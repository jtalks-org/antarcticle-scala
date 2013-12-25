package models.database

trait Schema extends ArticlesComponent with CommentsComponent with UsersComponent {
  this: Profile =>
}