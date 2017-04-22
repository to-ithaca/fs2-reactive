package fs2
package interop
package reactivestreams

import org.scalatest.testng.TestNGSuiteLike
import org.reactivestreams.tck.SubscriberWhiteboxVerification
import org.reactivestreams.tck.SubscriberBlackboxVerification
import org.reactivestreams.tck.TestEnvironment
import java.util.concurrent.atomic.AtomicInteger

import org.reactivestreams.tck.SubscriberWhiteboxVerification.{SubscriberPuppet, WhiteboxSubscriberProbe}
import org.reactivestreams._

import scala.concurrent.duration._

class SubscriberWhiteboxSpec extends SubscriberWhiteboxVerification[Int](new TestEnvironment(1000L)) with TestNGSuiteLike {
  implicit val S: Strategy = Strategy.fromFixedDaemonPool(1, "subscriber-spec")
  private val counter = new AtomicInteger()
  def createSubscriber(p: SubscriberWhiteboxVerification.WhiteboxSubscriberProbe[Int]): Subscriber[Int] =
    StreamSubscriber[Task, Int]().map { s =>
      new WhiteboxSubscriber(s, p)
    }.unsafeRun()

  def createElement(i: Int): Int = counter.getAndIncrement
}


final class WhiteboxSubscriber[A](sub: StreamSubscriber[Task, A],
  probe: WhiteboxSubscriberProbe[A]) extends Subscriber[A] {

  def onError(t: Throwable): Unit = {
    sub.onError(t)
    probe.registerOnError(t)
  }

  def onSubscribe(s: Subscription): Unit = {
    sub.onSubscribe(s)
    probe.registerOnSubscribe(new SubscriberPuppet {
      override def triggerRequest(elements: Long): Unit = {
        (0 to elements.toInt).foldLeft(Task.now[Unit](()))((t, _) => t.flatMap( _ => sub.sub.dequeue1.map(_ => ()))).unsafeRunAsync(_ => ())
      }

      override def signalCancel(): Unit = {
        s.cancel()
      }
    })
  }

  def onComplete(): Unit = {
    sub.onComplete()
    probe.registerOnComplete()
  }

  def onNext(a: A): Unit = {
    sub.onNext(a)
    probe.registerOnNext(a)
  }
}

class SubscriberBlackboxSpec extends SubscriberBlackboxVerification[Int](new TestEnvironment(1000L)) with TestNGSuiteLike {

  implicit val S: Strategy = Strategy.fromFixedDaemonPool(2, "subscriber-blackbox-spec")
  implicit val SS: Scheduler = Scheduler.fromFixedDaemonPool(2, "subscriber-blackbox-spec-scheduler")
  private val counter = new AtomicInteger()
  def createSubscriber(): StreamSubscriber[Task, Int] = StreamSubscriber[Task, Int]().unsafeRun()

  override def triggerRequest(s: Subscriber[_ >: Int]): Unit = {
    s.asInstanceOf[StreamSubscriber[Task, Int]].sub.dequeue1.schedule(100 milliseconds).unsafeRunAsync(_ => ())
  }

  def createElement(i: Int): Int = counter.incrementAndGet()
}
