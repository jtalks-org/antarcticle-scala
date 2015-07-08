package services

import akka.actor.ActorSystem

trait ActorSystemProvider {
  def actorSystem: ActorSystem
}

trait PlayActorSystemProvider extends ActorSystemProvider {
  import play.libs.Akka
  override def actorSystem: ActorSystem = Akka.system
}
