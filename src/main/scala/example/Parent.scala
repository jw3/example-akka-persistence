package example

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}
import example.Child.Unpersist
import example.Parent.{ChildCount, Children, CreateChild, RemoveChild}


object Parent {
  def props() = Props(new Parent)

  case class Children(list: Seq[String])
  case object ChildCount

  case class CreateChild(id: String)
  case class RemoveChild(id: String)
}

class Parent extends PersistentActor with ActorLogging {
  log.info("parent created")

  val persistenceId: String = "parent"

  val receiveRecover: Receive = {
    case SnapshotOffer(_, snapshot: Children) =>
      snapshot.list.map(Child.props).map(context.actorOf).foreach(_ ! Unpersist)
  }

  val receiveCommand: Receive = {
    case c@CreateChild(id) ⇒
      sender ! context.actorOf(Child.props(id), id)
      saveSnapshot(Children(childnames))

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
