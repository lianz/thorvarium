package actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Props


class UserActor(uuid: String, board: ActorRef, out: ActorRef) extends Actor with ActorLogging {

  override def preStart() = {
    BoardActor() ! Subscribe
  }

  def receive = LoggingReceive {
    case other => log.error(">>> Unhandled: " + other)
  }
}

object UserActor {
  def props(uuid: String)(out: ActorRef) = Props(new UserActor(uuid, BoardActor(), out))
}