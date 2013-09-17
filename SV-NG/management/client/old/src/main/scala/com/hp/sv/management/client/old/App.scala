
package com.hp.sv.management.client.old

import scala.io
import org.apache.commons.logging.{Log, LogFactory}
import scala.xml.XML
import java.io._
import java.net.{URLConnection, URL}
import resource._
import scala.collection.mutable.ArrayBuffer
import java.util.zip.{ZipEntry, ZipOutputStream}
import org.apache.commons.cli.{HelpFormatter, CommandLine, BasicParser, Options}
import scala.Predef._
import scala.Tuple3
import java.text.ParseException

object App {
  private val log: Log = LogFactory.getLog(classOf[App])

  val urlOptionName: String = "u"
  val zipOptionName: String = "z"

  val servicesUrl: String = "/services"
  val serviceUrl: String = "%s" + servicesUrl + "/%s?alt=xml"
  val virtualServiceFileExtensionPattern: String = "service-%s.vs"

  val performanceModelFileExtensionPattern: String = "performance-model-%s.vspfmodel"
  val performanceModelElementName: String = "performanceModel"
  val performanceModelUrl: String = "%s" + servicesUrl + "/%s/" + performanceModelElementName + "/%s"

  val serviceDescriptionFileExtensionPattern: String = "service-descripton-%s.vsdsc"
  val serviceDescriptionElementName: String = "serviceDescription"
  val serviceDescriptionUrl: String = "%s" + servicesUrl + "/%s/serviceDescriptions/%s"

  val dataModelFileExtensionPattern: String = "datamodel-%s.vsmodel"
  val dataModelElementName: String = "dataModel"
  val dataModelUrl: String = "%s" + servicesUrl + "/%s/" + dataModelElementName + "/%s?alt=xml"

  val datasetFileExtensionPattern: String = "dataset-%s.vsdataset"
  val datasetUrl: String = "%s" + servicesUrl + "/%s/" + dataModelElementName + "/%s/dataset/%s?alt=xml"

  val virtualServiceElementName: String = "virtualService"

  val refAttribute: String = "ref"
  val feedElementName: String = "feed"
  val entryElementName: String = "entry"
  val idUrl: String = "id"

  def main(args: Array[String]) {
    val (url: String, outputZipFile: String) = validateAndGetArgs(args)
    log.info(String.format("Url: %s", url));

    val file: File = new File(outputZipFile)
    if (file.exists())
      file.delete()

    managed(new FileOutputStream(outputZipFile)) acquireAndGet {
      fos => {
        managed(new ZipOutputStream(fos)) acquireAndGet {
          z => {
            val virtualServiceIds: Iterable[String] = (XML.load(io.Source.fromURL(url + servicesUrl).reader()) \\ feedElementName \\ entryElementName \\ idUrl).map(_.text)
            log.info(String.format("Found virtual service ids: %s", virtualServiceIds.mkString(", ")));

            val vsChildren = new ArrayBuffer[(String, String, String)]();

            virtualServiceIds.foreach(vsId => saveAndProcess(
              String.format(serviceUrl, url, vsId), z, String.format(virtualServiceFileExtensionPattern, vsId), (fs: InputStream) =>
                (XML.load(fs) \\ virtualServiceElementName \\ "_").
                  filter(n => n.label match {
                  case (`dataModelElementName` | `performanceModelElementName` | `serviceDescriptionElementName`) => true
                  case _ => false
                }).
                  foreach(n => vsChildren.append((vsId, n.label, n.attribute(refAttribute).get(0).text)))
            ))

            vsChildren.filter(e => e._2 == serviceDescriptionElementName).foreach(e => saveAndProcess(String.format(serviceDescriptionUrl, url, e._1, e._3), z,
              String.format(serviceDescriptionFileExtensionPattern, e._3), processAll))

            vsChildren.filter(e => e._2 == performanceModelElementName).foreach(e => saveAndProcess(String.format(performanceModelUrl, url, e._1, e._3), z,
              String.format(performanceModelFileExtensionPattern, e._3), processAll))

            val datasetIds: ArrayBuffer[(String, String, String)] = ArrayBuffer[(String, String, String)]()
            vsChildren.filter(e => e._2 == dataModelElementName).map(e => saveAndProcess(String.format(dataModelUrl, url, e._1, e._3), z, String.format(dataModelFileExtensionPattern, e._3),
              (fs: InputStream) => {
                (XML.load(fs) \\ dataModelElementName \\ "serviceOperationRules" \\ "serviceOperationRule" \\ "datasetIds" \\ "datasetId").
                  foreach(dId => datasetIds += new Tuple3[String, String, String](e._1, e._3, dId.text))
              }))

            datasetIds.foreach(e => saveAndProcess(String.format(datasetUrl, url, e._1, e._2, e._3), z, String.format(datasetFileExtensionPattern, e._3), processAll))
          }
        }
      }
    }
  }

  def processAll(i: InputStream) = Iterator.continually(i.read).takeWhile(-1 !=).foreach(ch => ch)

  def saveAndProcess(url: String, zip: ZipOutputStream, fileName: String, process: InputStream => Unit) = {
    log.info(String.format("Writing url content [%s] output to fileName [%s].", url, fileName))
    zip.putNextEntry(new ZipEntry(fileName))
    process(new InputStream {
      val i = getConnection(url).getInputStream

      def read(): Int = {
        val v: Int = i.read()
        if (v > -1) {
          zip.write(v)
        }
        v
      }
    })
  }

  private def getConnection(url: String): URLConnection = {
    val c: URLConnection = new URL(url).openConnection()
    c.setRequestProperty("Accept", "application/xml")
    c
  }

  def validateAndGetArgs(args: Array[String]): (String, String) = {
    var line: Option[CommandLine] = None
    try
      line = new Some(new BasicParser().parse(getOptions(), args))
    catch {
      case pe : org.apache.commons.cli.ParseException =>
        new HelpFormatter().printHelp("Runtime Report InMemory Rest Service", getOptions());
        throw pe
    }
    (line.get.getParsedOptionValue(urlOptionName).asInstanceOf[String], line.get.getParsedOptionValue(zipOptionName).asInstanceOf[String])
  }

  def getOptions(): Options = {
    val options: Options = new Options()

    val url = new org.apache.commons.cli.Option(urlOptionName, "url", true, "management server url")
    url.setRequired(true)
    url.setType(classOf[String])
    options.addOption(url)

    val zip = new org.apache.commons.cli.Option(zipOptionName, "zip", true, "output zip file name")
    zip.setRequired(true)
    zip.setType(classOf[String])
    options.addOption(zip)
  }
}