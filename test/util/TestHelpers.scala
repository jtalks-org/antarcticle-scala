package util

object TestHelpers {

  type :=>[A, B] = PartialFunction[A, B]

  def withHttp[T](response: String, port: Int)(doTest: => T) = {
    import akka.actor.ActorSystem
    import com.netaporter.precanned.dsl.basic._
    implicit val system = ActorSystem()
    val httpApi = httpServerMock(system).bind(port).block

    httpApi.expect(req => true).andRespondWith(entity(response))

    val result = doTest
    system.shutdown()
    result
  }
}
