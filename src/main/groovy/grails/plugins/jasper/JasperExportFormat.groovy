/* Copyright 2006-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package grails.plugins.jasper

import net.sf.jasperreports.engine.export.*
import net.sf.jasperreports.engine.export.oasis.JROdsExporter
import net.sf.jasperreports.engine.export.oasis.JROdtExporter
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
import net.sf.jasperreports.export.Exporter
import net.sf.jasperreports.export.ExporterConfiguration
import net.sf.jasperreports.export.ExporterOutput
import net.sf.jasperreports.export.ReportExportConfiguration
import net.sf.jasperreports.export.SimpleCsvExporterConfiguration
import net.sf.jasperreports.export.SimpleCsvReportConfiguration
import net.sf.jasperreports.export.SimpleDocxExporterConfiguration
import net.sf.jasperreports.export.SimpleDocxReportConfiguration
import net.sf.jasperreports.export.SimpleExporterConfiguration
import net.sf.jasperreports.export.SimpleHtmlExporterConfiguration
import net.sf.jasperreports.export.SimpleHtmlExporterOutput
import net.sf.jasperreports.export.SimpleHtmlReportConfiguration
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput
import net.sf.jasperreports.export.SimplePdfExporterConfiguration
import net.sf.jasperreports.export.SimplePdfReportConfiguration
import net.sf.jasperreports.export.SimpleReportExportConfiguration
import net.sf.jasperreports.export.SimpleRtfExporterConfiguration
import net.sf.jasperreports.export.SimpleRtfReportConfiguration
import net.sf.jasperreports.export.SimpleXlsExporterConfiguration
import net.sf.jasperreports.export.SimpleXlsReportConfiguration
import net.sf.jasperreports.export.SimpleXlsxExporterConfiguration
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration
import net.sf.jasperreports.export.SimpleXmlExporterOutput


/*
 * The supported file formats with their mimetype and file extension.
 * @author Sebastian Hohns
 */

enum JasperExportFormat implements Serializable {
  PDF_FORMAT("application/pdf", "pdf", false),
  HTML_FORMAT("text/html", "html", true),
  XML_FORMAT("text/xml", "xml", false),
  CSV_FORMAT("text/csv", "csv", false),
  XLS_FORMAT("application/vnd.ms-excel", "xls", false),
  RTF_FORMAT("text/rtf", "rtf", false),
  TEXT_FORMAT("text/plain", "txt", true),
  ODT_FORMAT("application/vnd.oasis.opendocument.text", "odt", false),
  ODS_FORMAT("application/vnd.oasis.opendocument.spreadsheetl", "ods", false),
  DOCX_FORMAT("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx", false),
  XLSX_FORMAT("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx", false),
  PPTX_FORMAT("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx", false)

  String mimeTyp
  String extension
  boolean inline

  private JasperExportFormat(String mimeTyp, String extension, boolean inline) {
    this.mimeTyp = mimeTyp
    this.extension = extension
    this.inline = inline
  }

  /**
   * Return the JasperExportFormat for a given format string.
   * @param format as String
   * @return JasperExportFormat
   */
  static JasperExportFormat determineFileFormat(String format) {
    switch (format) {
      case "PDF":  return JasperExportFormat.PDF_FORMAT
      case "HTML": return JasperExportFormat.HTML_FORMAT
      case "XML":  return JasperExportFormat.XML_FORMAT
      case "CSV":  return JasperExportFormat.CSV_FORMAT
      case "XLS":  return JasperExportFormat.XLS_FORMAT
      case "RTF":  return JasperExportFormat.RTF_FORMAT
      case "TEXT": return JasperExportFormat.TEXT_FORMAT
      case "ODT":  return JasperExportFormat.ODT_FORMAT
      case "ODS":  return JasperExportFormat.ODS_FORMAT
      case "DOCX": return JasperExportFormat.DOCX_FORMAT
      case "XLSX": return JasperExportFormat.XLSX_FORMAT
      case "PPTX": return JasperExportFormat.PPTX_FORMAT
      default: throw new RuntimeException("Invalid JasperExportFormat = ${format}")
    }
  }

  /**
   * Return the suitable Exporter for a given file format.
   * @param format
   * @return exporter
   */
  static Exporter getExporter(JasperExportFormat format) {
    switch (format) {
      case PDF_FORMAT:  return new JRPdfExporter()
      case HTML_FORMAT: return new HtmlExporter()
      case XML_FORMAT:  return new JRXmlExporter()
      case CSV_FORMAT:  return new JRCsvExporter()
      case XLS_FORMAT:  return new JRXlsExporter()
      case RTF_FORMAT:  return new JRRtfExporter()
      case TEXT_FORMAT: return new JRTextExporter()
      case ODT_FORMAT:  return new JROdtExporter()
      case ODS_FORMAT:  return new JROdsExporter()
      case DOCX_FORMAT: return new JRDocxExporter()
      case XLSX_FORMAT: return new JRXlsxExporter()
      case PPTX_FORMAT: return new JRPptxExporter()
      default: throw new RuntimeException("Invalid JasperExportFormat = ${format}")
    }
  }


  /**
   *
   * @param format
   * @return
   */
  static ExporterOutput getExporterOutput(JasperExportFormat format, ByteArrayOutputStream byteArrayOutputStream){
    switch (format) {
      case PDF_FORMAT:  return new SimpleOutputStreamExporterOutput(byteArrayOutputStream)
      case HTML_FORMAT: return new SimpleHtmlExporterOutput(byteArrayOutputStream)
      case XML_FORMAT:  return new SimpleXmlExporterOutput(byteArrayOutputStream)
      case CSV_FORMAT:  return new SimpleOutputStreamExporterOutput(byteArrayOutputStream)
      case XLS_FORMAT:  return new SimpleOutputStreamExporterOutput(byteArrayOutputStream)
      case RTF_FORMAT:  return new SimpleOutputStreamExporterOutput(byteArrayOutputStream)
      case TEXT_FORMAT: return new SimpleOutputStreamExporterOutput(byteArrayOutputStream)
      case ODT_FORMAT:  return new SimpleOutputStreamExporterOutput(byteArrayOutputStream)
      case ODS_FORMAT:  return new SimpleOutputStreamExporterOutput(byteArrayOutputStream)
      case DOCX_FORMAT: return new SimpleOutputStreamExporterOutput(byteArrayOutputStream)
      case XLSX_FORMAT: return new SimpleOutputStreamExporterOutput(byteArrayOutputStream)
      case PPTX_FORMAT: return new SimpleOutputStreamExporterOutput(byteArrayOutputStream)
      default: throw new RuntimeException("Invalid JasperExportFormat = ${format}")
    }
  }

  /**
   *
   * @param format
   * @return
   */
  static ExporterConfiguration getExporterConfiguration(JasperExportFormat format) {
    switch (format) {
      case PDF_FORMAT:  return new SimplePdfExporterConfiguration()
      case HTML_FORMAT: return new SimpleHtmlExporterConfiguration()
      case XML_FORMAT:  return new SimpleExporterConfiguration()
      case CSV_FORMAT:  return new SimpleCsvExporterConfiguration()
      case XLS_FORMAT:  return new SimpleXlsExporterConfiguration()
      case XLSX_FORMAT: return new SimpleXlsxExporterConfiguration()
      case RTF_FORMAT:  return new SimpleRtfExporterConfiguration()
      case DOCX_FORMAT: return new SimpleDocxExporterConfiguration()
      default: return null
    }
  }

  /**
   *
   * @param format
   * @return
   */
  static ReportExportConfiguration getReportConfiguration(JasperExportFormat format) {
    switch (format) {
      case PDF_FORMAT:  return new SimplePdfReportConfiguration()
      case HTML_FORMAT: return new SimpleHtmlReportConfiguration()
      case XML_FORMAT:  return new SimpleReportExportConfiguration()
      case CSV_FORMAT:  return new SimpleCsvReportConfiguration()
      case XLS_FORMAT:  return new SimpleXlsReportConfiguration()
      case XLSX_FORMAT: return new SimpleXlsxReportConfiguration()
      case RTF_FORMAT:  return new SimpleRtfReportConfiguration()
      case DOCX_FORMAT: return new SimpleDocxReportConfiguration()
      default: return null
    }
  }


  /**
   * Return the available ExporterConfiguration fields for a given JasperExportFormat.
   * @param format
   * @return List<MetaProperty> , null if no fields are available for the format
   */
    static List<MetaProperty> getExporterProperties(JasperExportFormat format) {
      List<MetaProperty> props
      Class clazz
      switch (format) {
        case PDF_FORMAT:
          clazz = SimplePdfExporterConfiguration
          break
        case HTML_FORMAT:
          clazz =  SimpleHtmlExporterConfiguration
          break
        case XML_FORMAT:
          clazz = SimpleExporterConfiguration
          break
        case CSV_FORMAT:
          clazz = SimpleCsvExporterConfiguration
          break
        case XLS_FORMAT:
          clazz = SimpleXlsExporterConfiguration
          break
        case XLSX_FORMAT:
          clazz = SimpleXlsxExporterConfiguration
          break
        case RTF_FORMAT:
          clazz = SimpleRtfExporterConfiguration
          break
        case DOCX_FORMAT:
          clazz = SimpleDocxExporterConfiguration
          break
        default: return null
      }
      props = clazz.metaClass.getProperties().findAll{((MetaBeanProperty)it).setter != null}
      return props
    }

  /**
   * Return the available ReportExportConfiguration fields for a given JasperExportFormat.
   * @param format
   * @return List<MetaProperty> , null if no fields are available for the format
   */
  static List<MetaProperty> getReportConfigurationProperties(JasperExportFormat format) {
    List<MetaProperty> props
    Class clazz
    switch (format) {
      case PDF_FORMAT:
        clazz = SimplePdfReportConfiguration
        break
      case HTML_FORMAT:
        clazz = SimpleHtmlReportConfiguration
        break
      case XML_FORMAT:
        clazz = SimpleReportExportConfiguration
        break
      case CSV_FORMAT:
        clazz = SimpleCsvReportConfiguration
        break
      case XLS_FORMAT:
        clazz = SimpleXlsReportConfiguration
        break
      case XLSX_FORMAT:
        clazz = SimpleXlsxReportConfiguration
        break
      case RTF_FORMAT:
        clazz = SimpleRtfReportConfiguration
        break
      case DOCX_FORMAT:
        clazz = SimpleDocxReportConfiguration
        break
      default: return null
    }
    props = clazz.metaClass.getProperties().findAll{((MetaBeanProperty)it).setter != null}
    return props
  }
}
