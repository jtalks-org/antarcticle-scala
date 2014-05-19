package models

object UserModels {
  case class UserModel(id: Int, username: String)
  case class UpdateUserRoleModel(id: Int, isAdmin: Boolean)
}
