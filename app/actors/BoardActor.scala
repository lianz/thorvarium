package actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Terminated
import models.User
import play.api.libs.json.{Json, JsValue}
import play.libs.Akka
import akka.actor.Props

class BoardActor extends Actor with ActorLogging {

  var games = Map.empty[String, ActorRef]
  var users = Map.empty[User, ActorRef]
  var invitations = Map.empty[User, User]

  def getGame(id: String): ActorRef = {
    games contains id match {
      case true => games(id)
      case false => log.info("== Create game id :: " + id + "==")
        val newGame = context.actorOf(Props(classOf[GameActor], id))
        games += (id -> newGame)
        newGame
    }
  }

  def receive = LoggingReceive {
    case message:Message => users foreach { _._2 ! message}

    case invite:Invitation =>
      users.find( u => u._1.id.get == invite.to ) match {
        case Some(x) =>

          invitations.find( u => u._1 == invite.from && u._2 == x._1 ) match {
            case Some(o) => log.error("== Invitation already made ==")
            case None =>
              invitations += (invite.from -> x._1)
              x._2 ! invite
          }

        case None => log.error("== Invite user not on board :: " + invite.from) + "=="
      }

    case accept:Accept =>
      users.find( u => u._1.id.get == accept.to ) match {
        case Some(x) =>

          users.find( u => u._1.id.get == accept.from.id.get ) match {
            case Some(y) =>

              invitations.find( u => u._2 == accept.from && u._1 == x._1 ) match {
                case Some(o) =>

                  invitations -= o._1
                  terminate(x)
                  terminate(y)

                  val game = getGame(o._1.id.get + "-" + o._2.id.get)
                  game ! SubscribeGame(x._1, x._2)
                  game ! SubscribeGame(y._1, y._2)

                case None => log.error("== Invitation not found ==")
              }

            case None => log.error("== Invited user not on board :: " + accept.from.id.get + "==")
          }
        case None => log.error("== Invited user not on board :: " + accept.to + "==")
      }

    case end:EndGame =>
      games -= end.id

    case subscribe:Subscribe =>

      log.info("== Subscribe user :: " + subscribe.user.nickname + " ==")

      users += (subscribe.user -> sender)
      context watch sender
      users foreach { _._2 ! members }

    case Terminated(user) => terminate(user)
  }

  def members : BoardMembers = {
    BoardMembers(Json.toJson(users.map( u => u._1.toJson )))
  }

  def terminate(node: (User, ActorRef)) : Unit = {

    log.info("== Unwatch user :: " + node._1.nickname + " ==")

    users -= node._1
    context unwatch node._2
    users foreach { _._2 ! members }

    invitations
        .filter( u => u._1.id == node._1.id || u._2.id == node._1.id )
        .foreach( invitations -= _._1 )
  }

  def terminate(ref : ActorRef) : Unit = {
    users.find( u => u._2 == ref ) match {
      case Some(x) => terminate(x)
      case None => log.error("== Terminated actor not found ==")
    }
  }
}

object BoardActor {
  lazy val board = Akka.system().actorOf(Props[BoardActor])
  def apply() = board
}

case class Message(user: User, message: String)
case class Invitation(from: User, to: Long)
case class Accept(from: User, to: Long)
case class BoardMembers(members: JsValue)
case class Subscribe(user: User)