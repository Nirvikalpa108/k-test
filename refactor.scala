import PaymentMethod.PaymentMethod

//import scala.collection.mutable.ListBuffer
import scala.collection.immutable.List

object PaymentMethod extends Enumeration {
  type PaymentMethod = Value
  val PAY_NOW, PAY_LATER, SLICE_IT = Value
}

class Event(val date: String, val amount: Long, val paymentMethod: PaymentMethod, val merchantId: String) {
  override def toString: String =
    "Event(" + date + "," + amount + "," + paymentMethod + "," + merchantId + ")"
}

class Aggregate(val datapoint: String, val events: Long) {
  override def toString: String =
    "Aggregate(" + datapoint + "," + events + ")"

  override def equals(obj: scala.Any): Boolean =
    obj match {
      case other: Aggregate => datapoint.equals(other.datapoint) && events.equals(other.events)
      case _: scala.Any => false
    }
}

object Aggregator {
  private def day(event: Event) = "^(.+)T.+$".r.replaceAllIn(event.date, "$1")

  private def hour(event: Event) = "^(.+)T(\\d+):.+$".r.replaceAllIn(event.date, "$1:$2")

  private def amountBracket(event: Event): String =
    if (event.amount < 1000) {
      "<10"
    } else if (event.amount < 5000) {
      "10-50"
    } else if (event.amount < 10000) {
      "50-100"
    } else if (event.amount < 50000) {
      "100-500"
    } else {
      ">500"
    }

  // can this output a type rather than side effect
  // use a more FP idiomatic loop
  // for each element in the list, check if it matches the datapoint parameter
  // if it does, change it's event paramater by adding 1
  private def addAggregate(aggregates: List[Aggregate], dp: String): List[Aggregate] = {
    aggregates.map { aggregate =>
      if (aggregate.datapoint.equals(dp)) new Aggregate(dp, aggregate.events + 1)
      else aggregate
    }
//     for (i <- 0 to aggregates.size - 1) {
//       if (aggregates(i).datapoint.equals(datapoint)) {
//         aggregates(i) = new Aggregate(datapoint, aggregates(i).events + 1)
//         return
//       }
//     }

//     aggregates += new Aggregate(datapoint, 1)
  }


  def aggregate(events: List[Event]): List[Aggregate] = {
    events.foldRight[List[Aggregate]](List.empty){ (event, acc) =>
      val agg = List(new Aggregate((""), 0.asInstanceOf[Long]))
      val a = addAggregate(agg, hour(event) + "|" + amountBracket(event))
      val b = addAggregate(agg, hour(event) + "|" + amountBracket(event) + "|" + event.paymentMethod)
      val c = addAggregate(agg, amountBracket(event) + "|" + event.paymentMethod)
      val d = addAggregate(agg, day(event) + "|" + event.merchantId)
      val e = addAggregate(agg, event.merchantId + "|" + event.paymentMethod)
      acc ++ a ++ b ++ c ++ d ++ e
    }
   
    
//     val aggregates: List[Aggregate] = ???

//     events.foreach(event => {
//       addAggregate(aggregates, hour(event) + "|" + amountBracket(event))
//       addAggregate(aggregates, hour(event) + "|" + amountBracket(event) + "|" + event.paymentMethod)
//       addAggregate(aggregates, amountBracket(event) + "|" + event.paymentMethod)
//       addAggregate(aggregates, day(event) + "|" + event.merchantId)
//       addAggregate(aggregates, event.merchantId + "|" + event.paymentMethod)
//     })

//     aggregates.toList
  }
}
