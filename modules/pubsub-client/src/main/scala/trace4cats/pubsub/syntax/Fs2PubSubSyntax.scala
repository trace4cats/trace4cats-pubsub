package trace4cats.pubsub.syntax

import cats.Monad
import cats.effect.MonadCancelThrow
import fs2.Stream
import fs2.pubsub._
import trace4cats.context.{Lift, Provide}
import trace4cats.fs2.TracedStream
import trace4cats.pubsub.{TracedPublisher, TracedSubscriber}
import trace4cats.{EntryPoint, ResourceKleisli, Span, SpanParams, ToHeaders, Trace}

trait Fs2PubSubSyntax {

  implicit class PublisherSyntax[F[_], A](publisher: PubSubPublisher[F, A]) {
    def liftTrace[G[_]](topic: Topic, projectId: ProjectId, toHeaders: ToHeaders = ToHeaders.standard)(implicit
      L: Lift[F, G],
      G: Monad[G],
      T: Trace[G]
    ): PubSubPublisher[G, A] =
      TracedPublisher.create[F, G, A](publisher, topic, projectId, toHeaders)
  }

  implicit class SubscriberSyntax[F[_], V](stream: Stream[F, PubSubRecord.Subscriber[F, V]]) {

    def inject[G[_]](ep: EntryPoint[F], subscription: Subscription)(implicit
      P: Provide[F, G, Span[F]],
      F: MonadCancelThrow[F],
      G: Monad[G],
      T: Trace[G],
    ): TracedStream[F, PubSubRecord.Subscriber[F, V]] =
      TracedSubscriber.inject[F, G, V](stream, subscription)(ep.toKleisli)

    def trace[G[_]](k: ResourceKleisli[F, SpanParams, Span[F]], subscription: Subscription)(implicit
      P: Provide[F, G, Span[F]],
      F: MonadCancelThrow[F],
      G: Monad[G],
      T: Trace[G],
    ): TracedStream[F, PubSubRecord.Subscriber[F, V]] =
      TracedSubscriber.inject[F, G, V](stream, subscription)(k)

    def injectK[G[_]](ep: EntryPoint[F], subscription: Subscription)(implicit
      P: Provide[F, G, Span[F]],
      F: MonadCancelThrow[F],
      G: MonadCancelThrow[G],
      trace: Trace[G]
    ): TracedStream[G, PubSubRecord.Subscriber[G, V]] =
      TracedSubscriber.injectK[F, G, V](stream, subscription)(ep.toKleisli)

    def traceK[G[_]](k: ResourceKleisli[F, SpanParams, Span[F]], subscription: Subscription)(implicit
      P: Provide[F, G, Span[F]],
      F: MonadCancelThrow[F],
      G: MonadCancelThrow[G]
    ): TracedStream[G, PubSubRecord.Subscriber[G, V]] =
      TracedSubscriber.injectK[F, G, V](stream, subscription)(k)
  }

}
