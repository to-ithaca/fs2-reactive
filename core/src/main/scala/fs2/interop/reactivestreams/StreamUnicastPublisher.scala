package fs2
package interop
package reactivestreams

import cats.effect._
import cats.implicits._
import org.reactivestreams._

import scala.concurrent.ExecutionContext

/** Implementation of an org.reactivestreams.Publisher.
  *
  * This is used to publish elements from an fs2.Stream to a downstream reactivestreams system.
  * 
  * @see https://github.com/reactive-streams/reactive-streams-jvm#1-publisher-code
  *
  */
final class StreamUnicastPublisher[F[_], A](val s: Stream[F, A])(implicit AA: Effect[F], ec: ExecutionContext) extends Publisher[A] {

  def subscribe(subscriber: Subscriber[_ >: A]): Unit = {
    nonNull(subscriber)
    async.unsafeRunAsync(StreamSubscription(subscriber, s).map { sub =>
      subscriber.onSubscribe(sub)
    })(_ => IO.unit)
  }

  private def nonNull[A](a: A): Unit = if(a == null) throw new NullPointerException()
}

object StreamUnicastPublisher {
  def apply[F[_], A](s: Stream[F, A])(implicit A: Effect[F], ec: ExecutionContext): StreamUnicastPublisher[F, A] =
    new StreamUnicastPublisher(s)
}
