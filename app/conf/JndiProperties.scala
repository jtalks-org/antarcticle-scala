package conf

import javax.naming.{Context, InitialContext}

class JndiProperties {
  private lazy val ctx = new InitialContext()
  private lazy val env = ctx.lookup("java:comp/env").asInstanceOf[Context]

  def apply[T](resource: String): T = env.lookup(resource).asInstanceOf[T]
}
