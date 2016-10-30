package example

import akka.actor.{ActorSystem, PoisonPill}
import akka.persistence.{RecoveryCompleted, SaveSnapshotSuccess}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import example.Child.{Activate, Activated, QueryState}
import org.scalatest._

import scala.concurrent.duration.DurationInt


class ChildSpec extends TestKit(ActorSystem()) with WordSpecLike with Matchers with EitherValues with ImplicitSender {
  implicit val timeout = Timeout(5 seconds)
  val ex = new RuntimeException("manually restarted")

  "a Child" should {
    "restart" when {
      "into the correct state" in {
        val c = childActorOf(Child.props("c1"))
        expectMsg(RecoveryCompleted)
        
        c ! Activate
        expectMsgType[SaveSnapshotSuccess]

        c ! QueryState
        expectMsg(Activated)

        watch(c)
        c ! PoisonPill
        expectTerminated(c)

        val r = childActorOf(Child.props("c1"))
        expectMsg(RecoveryCompleted)

        r ! QueryState
        expectMsg(Activated)
      }
    }
  }
}
