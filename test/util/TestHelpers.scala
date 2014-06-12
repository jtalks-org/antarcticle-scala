package util

import java.net.ServerSocket

object TestHelpers {

  def findFreePort() = {
    import resource._
    var port = -1
    for {
      socket <- managed(new ServerSocket(0))
    } {
      port = socket.getLocalPort
      socket.close()
    }
    port
  }

}
