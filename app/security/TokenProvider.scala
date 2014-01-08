package security

/*
 * Remember me token provider
 */
trait TokenProvider {
  def generateToken: String
}

class UUIDTokenProvider extends TokenProvider {
  import java.util.UUID

  def generateToken = UUID.randomUUID.toString
}
