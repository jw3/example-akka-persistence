package example

import akka.pattern.ask
import akka.actor.ActorSystem
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import example.Parent.CreateChild

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, DurationInt}


object Boot extends App with LazyLogging {
  implicit val timeout = Timeout(2 seconds)
  implicit val system = ActorSystem()
  import system.dispatcher

  val p1 = system.actorOf(Parent.props(), "p1")

  val a = p1 ? CreateChild("A")
  a.onComplete { _ ⇒
    childCount(p1)
    .map(c ⇒ logger.info(s"(1) child-count: $c"))
    childCount(p1)
  }

  Await.ready(a, Duration.Inf)

  system.stop(p1)

  Thread.sleep(1000)

  val p2 = system.actorOf(Parent.props(), "p2")
  childCount(p2)
  .map(c ⇒ logger.info(s"(2) child-count: $c"))


  val b = p2 ? CreateChild("B")
  b.onComplete { _ ⇒
    childCount(p2)
    .map(c ⇒ logger.info(s"(3) child-count: $c"))
  }

}
