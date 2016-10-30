package example

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.persistence.{RecoveryCompleted, SaveSnapshotSuccess}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import example.Child._
import example.Parent.{ChildCount, CreateChild}
import org.scalatest._

import scala.concurrent.duration.DurationInt

class ParentSpec extends TestKit(ActorSystem()) with WordSpecLike with Matchers with EitherValues with ImplicitSender {
  implicit val timeout = Timeout(5 seconds)
  val ex = new RuntimeException("manually restarted")

  "a Parent" should {
    "restart" when {
      "having no children" in {
        val p = childActorOf(Parent.props("p1"))
        expectMsg(RecoveryCompleted)

        watch(p)
        p ! PoisonPill
        expectTerminated(p)

        val r = childActorOf(Parent.props("p1"))
        expectMsg(RecoveryCompleted)

        r ! ChildCount
        expectMsg(0)
      }

      "having one child" in {
        val p = childActorOf(Parent.props("p2"))
        expectMsg(RecoveryCompleted)

        p ! CreateChild("c-2-1")
        expectMsgType[ActorRef]
        expectMsgType[SaveSnapshotSuccess]

        watch(p)
        p ! PoisonPill
        expectTerminated(p)

        val r = childActorOf(Parent.props("p2"))
        expectMsg(RecoveryCompleted)

        r ! ChildCount
        expectMsg(1)
      }

      "having multiple children" in {
        val p = childActorOf(Parent.props("p3"))
        expectMsg(RecoveryCompleted)

        p ! CreateChild("c-3-1")
        expectMsgType[ActorRef]
        expectMsgType[SaveSnapshotSuccess]

        p ! CreateChild("c-3-2")
        expectMsgType[ActorRef]
        expectMsgType[SaveSnapshotSuccess]

        watch(p)
        p ! PoisonPill
        expectTerminated(p)

        val r = childActorOf(Parent.props("p3"))
        expectMsg(RecoveryCompleted)

        r ! ChildCount
        expectMsg(2)
      }
    }
  }

  "nested children" should {
    "restart with parent into the correct state" in {
      val p = childActorOf(Parent.props("p1"))
      expectMsg(RecoveryCompleted)

      p ! CreateChild("c-3-1")
      val c1 = expectMsgType[ActorRef]
      expectMsgType[SaveSnapshotSuccess]

      c1 ! Activate
      expectMsgType[SaveSnapshotSuccess]

      c1 ! QueryState
      expectMsg(Activated)

      p ! CreateChild("c-3-2")
      expectMsgType[ActorRef]
      expectMsgType[SaveSnapshotSuccess]

      watch(p)
      p ! PoisonPill
      expectTerminated(p)

      val r = childActorOf(Parent.props("p1"))
      expectMsg(RecoveryCompleted)

      r ! QueryState
      expectMsgAllOf(Activated, Deactivated)
    }
  }
}
