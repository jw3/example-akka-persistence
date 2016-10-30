package example

import akka.actor.{ActorLogging, Props}
import akka.persistence._
import example.Child.{Activate, Deactivate, _}


object Child {
  def props(id: String) = Props(new Child(id))

  case object Activate
  case object Deactivate

  sealed trait ChildState
  case object Activated extends ChildState
  case object Deactivated extends ChildState

  case object QueryState
}

class Child(val persistenceId: String) extends PersistentActor with ActorLogging {
  log.info("child [{}] created", persistenceId)

  def receiveCommand: Receive = {
    case Activate ⇒
      saveSnapshot(Activated)
      context.become(active)

    case QueryState ⇒ sender ! Deactivated
  }

  def active: Receive = {
    case Deactivate ⇒
      saveSnapshot(Activated)
      context.become(inactive)

    case QueryState ⇒ sender ! Activated
    case r@SaveSnapshotSuccess(_) ⇒ context.parent ! r
    case r@SaveSnapshotFailure(_, _) ⇒ context.parent ! r
  }

  def inactive: Receive = {
    case Activate ⇒
      saveSnapshot(Deactivated)
      context.become(active)

    case QueryState ⇒ sender ! Deactivated
    case r@SaveSnapshotSuccess(_) ⇒ context.parent ! r
    case r@SaveSnapshotFailure(_, _) ⇒ context.parent ! r
  }

  def receiveRecover: Receive = {
    case RecoveryCompleted ⇒
      context.parent ! RecoveryCompleted

    case SnapshotOffer(_, state: ChildState) ⇒ state match {
      case Activated ⇒ context.become(active)
      case Deactivated ⇒ context.become(inactive)
    }
  }
}
