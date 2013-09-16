
package com.hp.sv.management

import scala.io
import org.apache.commons.lang3.Validate
import org.apache.commons.logging.{Log, LogFactory}
import scala.xml.XML
import scala.collection.immutable.Seq
import java.io._
import java.net.{URLConnection, URL}
import resource._

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

    virtualServiceIds.foreach(vsId => {
      managed(new ForkerStream(getConnection(String.format(serviceUrl, url, vsId)), String.format(virtualServiceFileExtensionPattern, outputDirectory, vsId))) acquireAndGet {
        vsO => {
          for (m <- XML.load(vsO) \\ virtualServiceElementName \\ "_") {
            val refId = m.attribute(refAttribute)
            m.label match {
              case `performanceModelElementName` =>
                save(getConnection(String.format(performanceModelUrl, url, vsId, refId.get(0).text)), String.format(performanceModelFileExtensionPattern, outputDirectory, refId.get(0).text))
              case `dataModelElementName` =>
                managed(new ForkerStream(getConnection(String.format(dataModelUrl, url, vsId, refId.get(0).text)), String.format(dataModelFileExtensionPattern, outputDirectory, refId.get(0).text))) acquireAndGet {
                  dmO => {
                    for (dm <- XML.load(dmO) \\ dataModelElementName \\ "serviceOperationRules" \\ "serviceOperationRule" \\ "datasetIds" \\ "datasetId") {
                      save(getConnection(String.format(datasetUrl, url, vsId, refId.get(0).text, dm.text)), String.format(datasetFileExtensionPattern, outputDirectory, dm.text))
                    }
                  }
                }
              case `serviceDescriptionElementName` =>
                save(getConnection(String.format(serviceDescriptionUrl, url, vsId, refId.get(0).text)), String.format(serviceDescriptionFileExtensionPattern, outputDirectory, refId.get(0).text))
              case _ =>
            }
          }
        }
      }
    })
  }

  def getConnection(url: String): URLConnection = {
    val c: URLConnection = new URL(url).openConnection()
    c.setRequestProperty("Accept", "application/xml")
    c
  }

  def save(url: URLConnection, file: String) {
    log.info(String.format("Writing url content [%s] output to file [%s].", url, file))
    var x: Int = 0
    managed(new ForkerStream(url, file)) acquireAndGet {
      f =>
        Iterator.continually(f.read()).takeWhile(-1 !=).foreach(ch => x += ch.toInt)
    }
    log.info(x)
  }
}

class ForkerStream(val url: URLConnection, file: String) extends InputStream {
  private val log: Log = LogFactory.getLog(classOf[ForkerStream])

  log.info(String.format("Writing url content [%s] output to file [%s].", url, file))
  val i = url.getInputStream
  val o = new FileOutputStream(file)

  def read(): Int = {
    val v: Int = i.read()
    if (v > -1) {
      o.write(v)
    }
    v
  }

  override def close() {
    o.close()
    super.close()
  }
}
