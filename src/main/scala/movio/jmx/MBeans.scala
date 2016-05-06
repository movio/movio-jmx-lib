package movio.jmx

import scala.collection.JavaConversions._
import scala.language.{ implicitConversions, postfixOps }
import scala.util.Try
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.lang.management.ManagementFactory
import javax.management._

trait Metric {
  def value: Long
}

object Metric {
  def apply(valueFn: ⇒ Long): Metric = new Metric {
    def value = valueFn
  }
}

object Implicits {

  implicit def atomicLongAsMetric(atomic: AtomicLong) = new Metric {
    def value = atomic.get
  }

  implicit def atomicIntegerAsMetric(atomic: AtomicInteger) = new Metric {
    def value = atomic.get
  }

}

class MetricsMBean(attributes: Map[String, Metric], description: String) extends DynamicMBean {

  override def getAttribute(name: String) =
    attributes.get(name).map(_.value.asInstanceOf[java.lang.Long]).getOrElse {
      throw new AttributeNotFoundException(s"Attribute $name not found")
    }

  override def getAttributes(names: Array[String]) =
    new AttributeList(
      for {
        name   ← names.toSeq
        metric ← attributes.get(name)
      } yield {
        new Attribute(name, metric.value.asInstanceOf[java.lang.Long])
      }
    )

  // It's good practice to instantiate MBeanInfo only once.
  // See http://docs.oracle.com/cd/E19698-01/816-7609/6mdjrf83d/index.html#dynamic-14
  override val getMBeanInfo = {
    val attrInfos = attributes.keys.map { name ⇒
      new MBeanAttributeInfo(name, "long", s"Metric $name", true, false, false)
    } toArray

    new MBeanInfo(
      classOf[MetricsMBean].getName(),
      description,
      attrInfos,
      null,
      null,
      null
    )
  }

  override def setAttribute(attribute: Attribute) =
    throw new UnsupportedOperationException("Setting attribute is not supported")

  override def setAttributes(attributes: AttributeList) =
    throw new UnsupportedOperationException("Setting attribute is not supported")

  override def invoke(actionName: String, params: Array[Object], signature: Array[String]) =
    throw new UnsupportedOperationException("Invoking is not supported")

}

object ObjectName {
  def apply(name: String) = new ObjectName(name)
}

object MetricsMBean {

  lazy val server = ManagementFactory.getPlatformMBeanServer

  def create(name: ObjectName, attributes: Map[String, Metric], description: String = ""): Try[MetricsMBean] =
    Try {
      val mbean = new MetricsMBean(attributes, description)
      server.registerMBean(mbean, name)
      mbean
    }

  def remove(name: ObjectName): Try[Unit] =
    Try {
      server.unregisterMBean(name)
    }

}
