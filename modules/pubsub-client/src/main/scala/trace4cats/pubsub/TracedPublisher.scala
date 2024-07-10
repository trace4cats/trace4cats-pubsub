package trace4cats.pubsub

import cats.syntax.all._
import cats.Monad
import trace4cats.{SpanKind, ToHeaders, Trace}
import fs2.pubsub.{MessageId, ProjectId, PubSubPublisher, PubSubRecord, Topic}
import trace4cats.context.Lift
import trace4cats.model.TraceHeaders

object TracedPublisher {

  def create[F[_], G[_]: Monad: Trace, A](
    producer: PubSubPublisher[F, A],
    topic: Topic,
    projectId: ProjectId,
    toHeaders: ToHeaders = ToHeaders.standard
  )(implicit L: Lift[F, G]): PubSubPublisher[G, A] = new PubSubPublisher[G, A] {

    override def publishMany(records: Seq[PubSubRecord.Publisher[A]]): G[List[MessageId]] =
      Trace[G].span("pubsub.publish", SpanKind.Producer) {
        Trace[G].headers(toHeaders).flatMap { traceHeaders =>
          Trace[G].putAll("topic" -> topic.value, "project_id" -> projectId.value) >> L.lift(
            producer.publishMany(addHeaders(traceHeaders)(records))
          )
        }
      }

    def addHeaders(headers: TraceHeaders)(records: Seq[PubSubRecord.Publisher[A]]): Seq[PubSubRecord.Publisher[A]] = {
      val pubSubHeaders = PubSubHeaders.converter.to(headers)
      records.map(_.withAttributes(pubSubHeaders))
    }
  }

}
