
package com.hp.sv.management

import scala.io
import org.apache.commons.lang3.Validate
import org.apache.commons.logging.{Log, LogFactory}
import scala.xml.{Node, XML}
import scala.collection.immutable.Seq
import java.io._
import java.net.{URLConnection, URL}
import resource._
import scala.collection.mutable.ArrayBuffer

object App {
  private val log: Log = LogFactory.getLog(classOf[App])

  val outputDirectory = new File(System.getProperty("user.dir"), "target/project")
  outputDirectory.mkdir()

  val servicesUrl: String = "/services"
  val serviceUrl: String = "%s" + servicesUrl + "/%s?alt=xml"
  val virtualServiceFileExtensionPattern: String = "%s/service-%s.vs"

  val performanceModelFileExtensionPattern: String = "%s/performance-model-%s.vspfmodel"
  val performanceModelElementName: String = "performanceModel"
  val performanceModelUrl: String = "%s" + servicesUrl + "/%s/" + performanceModelElementName + "/%s"

  val serviceDescriptionFileExtensionPattern: String = "%s/service-descripton-%s.vsdsc"
  val serviceDescriptionElementName: String = "serviceDescription"
  val serviceDescriptionUrl: String = "%s" + servicesUrl + "/%s/serviceDescriptions/%s"

  val dataModelFileExtensionPattern: String = "%s/datamodel-%s.vsmodel"
  val dataModelElementName: String = "dataModel"
  val dataModelUrl: String = "%s" + servicesUrl + "/%s/" + dataModelElementName + "/%s?alt=xml"

  val datasetFileExtensionPattern: String = "%s/dataset-%s.vsdataset"
  val datasetUrl: String = "%s" + servicesUrl + "/%s/" + dataModelElementName + "/%s/dataset/%s?alt=xml"

  val virtualServiceElementName: String = "virtualService"

  val refAttribute: String = "ref"
  val feedElementName: String = "feed"
  val entryElementName: String = "entry"
  val idUrl: String = "id"

  def main(args: Array[String]) {
    Validate.notEmpty(args);
    val Array(url: String) = args;

    log.info(String.format("Url: %s", url));

    val virtualServiceIds: Seq[String] = (XML.load(io.Source.fromURL(url + servicesUrl).reader()) \\ feedElementName \\ entryElementName \\ idUrl).map(_.text)
    log.info(String.format("Found virtual service ids: %s", virtualServiceIds.mkString(", ")));

    val dataModelElements: ArrayBuffer[(String, String)] = new ArrayBuffer[(String, String)]()
    val performanceModelElements: ArrayBuffer[(String, String)] = new ArrayBuffer[(String, String)]()
    val sdElements: ArrayBuffer[(String, String)] = new ArrayBuffer[(String, String)]()

    virtualServiceIds.foreach(vsId => saveAndProcess(
      String.format(serviceUrl, url, vsId), String.format(virtualServiceFileExtensionPattern, outputDirectory, vsId), (fs: InputStream) => {
        for (m <- XML.load(fs) \\ virtualServiceElementName \\ "_") m.label match {
          case `dataModelElementName` => dataModelElements += new Tuple2[String, String](vsId, m.attribute(refAttribute).get(0).text)
          case `performanceModelElementName` => performanceModelElements += new Tuple2[String, String](vsId, m.attribute(refAttribute).get(0).text)
          case `serviceDescriptionElementName` => sdElements += new Tuple2[String, String](vsId, m.attribute(refAttribute).get(0).text)
          case _ =>
        }
      }
    ))

    sdElements.foreach(e => saveAndProcess(String.format(serviceDescriptionUrl, url, e._1, e._2),
      String.format(serviceDescriptionFileExtensionPattern, outputDirectory, e._2), processAll))

    performanceModelElements.foreach(e => saveAndProcess(String.format(performanceModelUrl, url, e._1, e._2),
      String.format(performanceModelFileExtensionPattern, outputDirectory, e._2), processAll))

    val datasetIds: ArrayBuffer[(String, String, String)] = ArrayBuffer[(String, String, String)]()
    dataModelElements.map(e => saveAndProcess(String.format(dataModelUrl, url, e._1, e._2), String.format(dataModelFileExtensionPattern, outputDirectory, e._2),
      (fs: InputStream) => {
        (XML.load(fs) \\ dataModelElementName \\ "serviceOperationRules" \\ "serviceOperationRule" \\ "datasetIds" \\ "datasetId").
          foreach(dId => datasetIds += new Tuple3[String, String, String](e._1, e._2, dId.text))
      }))

    datasetIds.foreach(e => saveAndProcess(String.format(datasetUrl, url, e._1, e._2, e._3),
      String.format(datasetFileExtensionPattern, outputDirectory, e._3), processAll))
  }

  def saveAndProcess(url: String, file: String, process: InputStream => Unit) = {
    managed(new ForkerStream(url, file)) acquireAndGet {
      fs => process(fs)
    }
  }

  def processAll(i: InputStream) = Iterator.continually(i.read).takeWhile(-1 !=).foreach(ch => ch)
}

class ForkerStream(val url: String, file: String) extends InputStream {
  private val log: Log = LogFactory.getLog(classOf[ForkerStream])

  log.info(String.format("Writing url content [%s] output to file [%s].", url, file))
  val i = getConnection(url).getInputStream
  val o = new FileOutputStream(file)

  def read(): Int = {
    val v: Int = i.read()
    if (v > -1) {
      o.write(v)
    }
    v
  }

  private def getConnection(url: String): URLConnection = {
    val c: URLConnection = new URL(url).openConnection()
    c.setRequestProperty("Accept", "application/xml")
    c
  }

  override def close() {
    o.close()
    super.close()
  }
}
