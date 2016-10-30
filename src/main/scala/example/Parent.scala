package example

import akka.actor.{ActorLogging, Props}
import akka.persistence._
import example.Parent._


object Parent {
  def props(pid: String) = Props(new Parent(pid))

  case class Children(list: Seq[String])
  case object ChildCount

  case class CreateChild(id: String)
  case class RemoveChild(id: String)
}

class Parent(val persistenceId: String) extends PersistentActor with ActorLogging {
  log.info("parent actor {} created", persistenceId)

  val receiveRecover: Receive = {
    case RecoveryCompleted ⇒
      context.parent ! RecoveryCompleted

    case SnapshotOffer(_, snapshot: Children) =>
      log.info("restoring snapshot, {}", snapshot)
      snapshot.list.map(Child.props).map(context.actorOf)
  }

  val receiveCommand: Receive = {
    case c@CreateChild(id) ⇒
      val child = context.actorOf(Child.props(id), id)
      saveSnapshot(Children(childnames))
      sender ! child

    case r: SaveSnapshotSuccess ⇒
      context.parent ! r

    case r: SaveSnapshotFailure ⇒
      context.parent ! r

    case c@RemoveChild(id) ⇒
      context.child(id).foreach { c ⇒
        context.stop(c)
        saveSnapshot(Children(childnames))
      }

    case ChildCount ⇒
      sender ! context.children.size
  }

  def childnames: Seq[String] = context.children.seq.map(_.path.name).toSeq
}
