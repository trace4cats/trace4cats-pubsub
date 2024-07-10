package trace4cats.pubsub

import cats.syntax.all._
import cats.Monad
import cats.effect.MonadCancelThrow
import trace4cats.{AttributeValue, ResourceKleisli, Span, SpanParams, Trace}
import fs2.Stream
import fs2.pubsub.PubSubRecord
import trace4cats.context.Provide
import trace4cats.fs2.TracedStream
import trace4cats.fs2.syntax.Fs2StreamSyntax
import trace4cats.model.SpanKind
import fs2.pubsub.Subscription

object TracedSubscriber extends Fs2StreamSyntax {

  def inject[F[_]: MonadCancelThrow, G[_]: Monad: Trace, V](
    stream: Stream[F, PubSubRecord.Subscriber[F, V]],
    subscription: Subscription
  )(
    k: ResourceKleisli[F, SpanParams, Span[F]]
  )(implicit P: Provide[F, G, Span[F]]): TracedStream[F, PubSubRecord.Subscriber[F, V]] =
    stream
      .traceContinue(k, "pubsub.receive", SpanKind.Consumer) { record =>
        PubSubHeaders.converter.from(record.attributes)
      }
      .evalMapTrace { record =>
        Trace[G].put("subscription", subscription.value) >>
          record.publishTime.traverse_(publishTime =>
            Trace[G].put("publish_time", AttributeValue.LongValue(publishTime.toEpochMilli))
          ) >>
          record.messageId.traverse_(messageId => Trace[G].put("message_id", messageId.value)).as(record)
      }

  def injectK[F[_]: MonadCancelThrow, G[_]: MonadCancelThrow: Trace, V](
    stream: Stream[F, PubSubRecord.Subscriber[F, V]],
    subscription: Subscription,
  )(
    k: ResourceKleisli[F, SpanParams, Span[F]]
  )(implicit P: Provide[F, G, Span[F]]): TracedStream[G, PubSubRecord.Subscriber[G, V]] = {
    def liftSubscriptionRecord(record: PubSubRecord.Subscriber[F, V]): PubSubRecord.Subscriber[G, V] =
      PubSubRecord.Subscriber[G, V](
        record.value,
        record.attributes,
        record.messageId,
        record.publishTime,
        record.ackId,
        P.lift(record.ack),
        P.lift(record.nack),
        deadline => P.lift(record.extendDeadline(deadline))
      )

    inject[F, G, V](stream, subscription)(k).liftTrace[G].map(liftSubscriptionRecord)
  }
}
