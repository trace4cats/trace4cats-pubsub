package trace4cats.pubsub

import trace4cats.model.TraceHeaders

object PubSubHeaders {

  val converter: TraceHeaders.Converter[Map[String, String]] = new TraceHeaders.Converter[Map[String, String]] {
    def from(t: Map[String, String]): TraceHeaders =
      TraceHeaders.of(t.toSeq: _*)
    def to(h: TraceHeaders): Map[String, String] =
      h.values.view.map { case (k, v) => k.toString -> v }.toMap
  }

}
