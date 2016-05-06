package movio.jmx

import scala.util.{ Success, Failure }
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

import org.mockito.Matchers.any
import org.scalatest.FunSpecLike
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar


class MetricsMBeanTest extends FunSpecLike with MockitoSugar {

  describe("Metric MBean") {

    it("supports using a closure as a metric source") {
      new Fixture {
        testMBean.getAttribute("metric1") shouldBe 10
        metric1Val = 20
        testMBean.getAttribute("metric1") shouldBe 20
      }
    }

    it("supports using AtomicLong as a metric source") {
      new Fixture {
        testMBean.getAttribute("metric2") shouldBe 0
        metric2Val.addAndGet(99)
        testMBean.getAttribute("metric2") shouldBe 99
      }
    }

    it("supports using AtomicInteger as a metric source") {
      new Fixture {
        testMBean.getAttribute("metric3") shouldBe 0
        metric3Val.addAndGet(99)
        testMBean.getAttribute("metric3") shouldBe 99
      }
    }

    it("supports getting multiple attributes") {
      import scala.collection.JavaConversions._
      new Fixture {
        val attrs = testMBean.getAttributes(Array("metric1", "metric2", "unknown")).asList
        attrs should have size 2
        attrs(0).getName shouldBe "metric1"
        attrs(0).getValue shouldBe 10
        attrs(1).getName shouldBe "metric2"
        attrs(1).getValue shouldBe 0
      }
    }

    it("doesn't register a MBean twice") {
      val name = ObjectName("atm.test:type=Demo")
      MetricsMBean.create(name, Map("foo" → Metric(100))) should be a 'success
      MetricsMBean.create(name, Map("foo" → Metric(100))) should be a 'failure

      MetricsMBean.remove(name)
    }

    it("fails to remove a non-existent MBean") {
      val name = ObjectName("atm.unknown:type=Foo")
      MetricsMBean.remove(name) should be a 'failure
    }

  }

  trait Fixture {
    import Implicits._

    var metric1Val: Long = 10
    val metric2Val = new AtomicLong
    val metric3Val = new AtomicInteger
    val testMBean = new MetricsMBean(
      Map(
        "metric1" → Metric(metric1Val),
        "metric2" → metric2Val,
        "metric3" → metric3Val
      ),
      "test mbean"
    )
  }

}
