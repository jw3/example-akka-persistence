package example

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, SnapshotOffer, SnapshotSelectionCriteria}
import example.Child._


object Child {
  def props(id: String) = Props(new Child(id))

  case class Activate()
  case class Deactivate()

  sealed trait ChildState
  case object Activated extends ChildState
  case object Deactivated extends ChildState

  case object Unpersist
}

class Child(val persistenceId: String) extends PersistentActor with ActorLogging {
  log.info("child [{}] created", persistenceId)

  def receiveCommand: Receive = {
    case Unpersist ⇒
      loadSnapshot(persistenceId, SnapshotSelectionCriteria.Latest, Long.MaxValue)

    case Activate() ⇒
      context.become(active)
  }

  def active: Receive = {
    saveSnapshot(Activated)

    {
      case Deactivate() ⇒
        context.become(inactive)
    }
  }

  def inactive: Receive = {
    saveSnapshot(Deactivated)

    {
      case Activate() ⇒
        context.become(active)
    }
  }

  def receiveRecover: Receive = {
    case SnapshotOffer(_, state: ChildState) ⇒ state match {
      case Activated ⇒ context.become(active)
      case Deactivated ⇒ context.become(inactive)
    }
  }
}
