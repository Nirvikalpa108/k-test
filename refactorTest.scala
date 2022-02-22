import org.junit.Assert.assertTrue
import org.junit.Test

class AggregatorTest {

  private val sampleEvent: Event = new Event(
    "2011-12-03T10:15:30Z",
    4285,
    PaymentMethod.SLICE_IT,
    "1bb53ed1-787b-4543-9def-ea18eef7902e"
  )

  private def assertAggregates(expected: List[Aggregate], actual: List[Aggregate]): Unit = {
    expected.foreach(aggregate => assertTrue("Missing expected " + aggregate, actual.contains(aggregate)))
  }

  @Test def oneEvent = assertAggregates(
    List(
      new Aggregate("2011-12-03:10|10-50", 1),
      new Aggregate("2011-12-03:10|10-50|SLICE_IT", 1),
      new Aggregate("10-50|SLICE_IT", 1),
      new Aggregate("2011-12-03|1bb53ed1-787b-4543-9def-ea18eef7902e", 1),
      new Aggregate("1bb53ed1-787b-4543-9def-ea18eef7902e|SLICE_IT", 1)
    ),
    Aggregator.aggregate(List(sampleEvent))
  )

  @Test def repeatedEvents = assertAggregates(
    List(
      new Aggregate("2011-12-03:10|10-50", 2),
      new Aggregate("2011-12-03:10|10-50|SLICE_IT", 2),
      new Aggregate("10-50|SLICE_IT", 2),
      new Aggregate("2011-12-03|1bb53ed1-787b-4543-9def-ea18eef7902e", 2),
      new Aggregate("1bb53ed1-787b-4543-9def-ea18eef7902e|SLICE_IT", 2)
    ),
    Aggregator.aggregate(List(sampleEvent, sampleEvent))
  )

  @Test def oneMerchantDifferentHoursAmountsAndPaymentMethods = {
    assertAggregates(
      List(
        new Aggregate("10-50|PAY_NOW", 1),
        new Aggregate("10-50|SLICE_IT", 1),
        new Aggregate("1bb53ed1-787b-4543-9def-ea18eef7902e|PAY_LATER", 1),
        new Aggregate("1bb53ed1-787b-4543-9def-ea18eef7902e|PAY_NOW", 2),
        new Aggregate("1bb53ed1-787b-4543-9def-ea18eef7902e|SLICE_IT", 1),
        new Aggregate("2011-12-03:10|10-50", 1),
        new Aggregate("2011-12-03:10|10-50|SLICE_IT", 1),
        new Aggregate("2011-12-03:12|10-50", 1),
        new Aggregate("2011-12-03:12|10-50|PAY_NOW", 1),
        new Aggregate("2011-12-03:14|<10", 1),
        new Aggregate("2011-12-03:14|<10|PAY_NOW", 1),
        new Aggregate("2011-12-03|1bb53ed1-787b-4543-9def-ea18eef7902e", 3),
        new Aggregate("2011-12-04:10|>500", 1),
        new Aggregate("2011-12-04:10|>500|PAY_LATER", 1),
        new Aggregate("2011-12-04|1bb53ed1-787b-4543-9def-ea18eef7902e", 1),
        new Aggregate("<10|PAY_NOW", 1),
        new Aggregate(">500|PAY_LATER", 1)
      ),
      Aggregator.aggregate(
        List(
          new Event("2011-12-03T10:15:30Z", 4285, PaymentMethod.SLICE_IT, "1bb53ed1-787b-4543-9def-ea18eef7902e"),
          new Event("2011-12-03T12:15:30Z", 1142, PaymentMethod.PAY_NOW, "1bb53ed1-787b-4543-9def-ea18eef7902e"),
          new Event("2011-12-03T14:15:30Z", 185, PaymentMethod.PAY_NOW, "1bb53ed1-787b-4543-9def-ea18eef7902e"),
          new Event("2011-12-04T10:15:30Z", 82850, PaymentMethod.PAY_LATER, "1bb53ed1-787b-4543-9def-ea18eef7902e")
        )
      )
    )
  }
}
