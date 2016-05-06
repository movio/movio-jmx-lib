package movio.jmx

import java.util.concurrent.atomic.AtomicLong

object Demo {

  import Implicits._

  def main(args: Array[String]): Unit = {

    val counter = new AtomicLong

    MetricsMBean.create(
      ObjectName("movio.cinema.test:type=Foobar"),
      Map(
        "currentTime" → Metric { System.currentTimeMillis },
        "counter"     → counter
      )
    )

    while(true) {
      Thread.sleep(500)
      counter.incrementAndGet()
    }

  }

}
