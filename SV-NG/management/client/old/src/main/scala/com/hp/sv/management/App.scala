
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
  val virtualServiceFileExtensionPattern: String = "service-%s.vs"

  val performanceModelFileExtensionPattern: String = "performance-model-%s.vspfmodel"
  val performanceModelElementName: String = "performanceModel"
  val performanceModelUrl: String = "%s" + servicesUrl + "/%s/" + performanceModelElementName + "/%s"

  val virtualServiceElementName: String = "virtualService"
  val dataModelElementName: String = "dataModel"

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
      managed(new FileOutputStream(new File(outputDirectory, String.format(virtualServiceFileExtensionPattern, vsId)))) acquireAndGet {
        o => {
          for (m <- XML.load(new ForkerStream(getConnection(String.format(serviceUrl, url, vsId)).getInputStream, o)) \\ virtualServiceElementName \\ "_") {
            val refId = m.attribute(refAttribute)
            m.label match {
              case `performanceModelElementName` =>
                save(getConnection(String.format(performanceModelUrl, url, vsId, refId.get(0).text)), new File(outputDirectory, String.format(performanceModelFileExtensionPattern, refId.get(0).text)))
              case `dataModelElementName` =>

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

  def save(url: URLConnection, outputFile: File) {
    log.info(String.format("Writing url content [%s] output to file [%s].", url, outputFile))
    for (i <- managed(url.getInputStream); o <- managed(new FileOutputStream(outputFile))) {
      Iterator.continually(i.read()).takeWhile(-1 !=).foreach(o.write)
    }
  }
}

class ForkerStream(val i: InputStream, o: OutputStream) extends InputStream {
  def read(): Int = {
    val v: Int = i.read()
    if (v > -1) {
      o.write(v)
    }
    v
  }
}
