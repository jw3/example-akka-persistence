import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import example.Parent.ChildCount

import scala.concurrent.{ExecutionContext, Future}

package object example {

  def childCount(parent: ActorRef)(implicit ec: ExecutionContext, to: Timeout): Future[Int] = {
    (parent ? ChildCount).mapTo[Int].map {
      case v: Int ⇒ v
    }
  }

}
