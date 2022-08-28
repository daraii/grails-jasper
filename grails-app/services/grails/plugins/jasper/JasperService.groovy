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

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import net.sf.jasperreports.engine.*
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import net.sf.jasperreports.export.CommonExportConfiguration
import net.sf.jasperreports.export.Exporter
import net.sf.jasperreports.export.ExporterConfiguration
import net.sf.jasperreports.export.ReportExportConfiguration
import net.sf.jasperreports.export.SimpleExporterInput
import org.springframework.core.io.Resource
import static grails.plugins.jasper.JasperPluginUtils.TEMP_COMPILE_SUBDIRECTORY
import java.sql.Connection

/**
 * Generates Jasper reports. Call one of the three generateReport methods to
 * get a ByteArrayOutputStream with the generated report.
 * @author Sebastian Hohns
 */
@Transactional(readOnly = true)
class JasperService {

    def dataSource

    static final boolean FORCE_TEMP_DIRECTORY = false

    /**
     * Build a JasperReportDef form a parameter map. This is used by the taglib.
     * @param parameters
     * @param locale
     * @param testModel
     * @return reportDef
     */
    JasperReportDef buildReportDefinition(parameters, locale, testModel) {
        JasperReportDef reportDef = new JasperReportDef(name: parameters._file, parameters: parameters,locale: locale)

        reportDef.fileFormat = JasperExportFormat.determineFileFormat(parameters._format)
        reportDef.reportData = getReportData(testModel, parameters)
        reportDef.contentStream = generateReport(reportDef)
        reportDef.jasperPrinter = generatePrinter(reportDef)

        return reportDef
    }

    private Collection getReportData(testModel, parameters) {
        Collection reportData

        if (testModel?.data) {
            try {
                reportData = testModel.data
            } catch (Throwable e) {
                throw new Exception("Expected chainModel data parameter to be a Collection, but it was ${testModel.data.class.name}", e)
            }
        }
        else {
            testModel = getProperties().containsKey('model') ? model : null //?
            if (testModel?.data) {
                try {
                    reportData = testModel.data
                } catch (Throwable e) {
                    throw new Exception("Expected model.data parameter to be a Collection, but it was ${model.data.class.name}", e)
                }
            } else if (parameters?.data) {
                try {
                    reportData = parameters.data
                } catch (Throwable e) {
                    throw new Exception("Expected data parameter to be a Collection, but it was ${parameters.data.class.name}", e)
                }
            }
        }

        return reportData
    }

    @Deprecated
    ByteArrayOutputStream generateReport(String jasperReportPath, JasperExportFormat format, Collection reportData, Map parameters) {
        JasperReportDef reportDef = new JasperReportDef(name: parameters._file, folder: jasperReportPath, reportData: reportData, fileFormat: format,
                parameters: parameters)
        return generateReport(reportDef)
    }

    /**
     * Generate a report based on a single jasper file.
     * @param format , target format
     * @param reportDef , jasper report object
     * return ByteArrayOutStreamByteArrayOutStream with the generated Report
     */
    ByteArrayOutputStream generateReport(JasperReportDef reportDef) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream()
        def exporter = generateExporter(reportDef)
        def exporterConfig = getExporterConfiguration(reportDef)
        def reportExportConfig = getExportReportConfiguration(reportDef)
        exporter.setConfiguration(reportExportConfig)
        exporter.setConfiguration(exporterConfig)
        exporter.setExporterOutput(JasperExportFormat.getExporterOutput(reportDef.fileFormat, byteArray))

        def jasperPrint = reportDef.jasperPrinter
        if (jasperPrint==null) {
            reportDef.jasperPrinter = generatePrinter(reportDef)
        }
        exporter.setExporterInput(new SimpleExporterInput(reportDef.jasperPrinter))
        reportDef.reportExportConfiguration = reportExportConfig
        reportDef.exporterConfiguration = exporterConfig
        exporter.exportReport()

        return byteArray
    }

    /**
     * Generate a single report based on a list of jasper files.
     * @param reports , a List with report objects
     * return ByteArrayOutStream with the generated Report
     */
    ByteArrayOutputStream generateReport(List<JasperReportDef> reports) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream()
        Exporter exporter = generateExporter(reports.first())
        def format = reports.first().fileFormat
        exporter.setExporterOutput(JasperExportFormat.getExporterOutput(format, byteArray))
        def printers = reports.collect { report -> generatePrinter(report) }
        exporter.setExporterInput(new SimpleExporterInput(printers))
        reports*.exporterConfiguration = exporter
        exporter.exportReport()

        return byteArray
    }

    /**
     * Forces the Jasper Reports  temp folder to be "~/.grails/.jasper" and ensures that such a folder exists.
     * The user (however the app server is logged in) is much more likely to have read/write/delete rights here than the
     * default location that Jasper Reports uses.
     */
    protected void forceTempFolder() {
        /* TODO This is currently disabled, because it doesn't work. Jasper Reports seems to always use the current
         * folder (.) no matter what.  (I'll be filing a bug report against Jasper Reports itself shortly - Craig Jones 16-Aug-2008)
         */
        if (FORCE_TEMP_DIRECTORY) {
            // Look up the home folder explicitly (don't trust that tilde notation will work).
            String userHomeDir = System.getProperty('user.home')
            File tempFolder = new File(userHomeDir, TEMP_COMPILE_SUBDIRECTORY)

           // Sets property if not already set.
            if(!System.getProperty("net.sf.jasperreports.compiler.temp.dir")) {
                System.setProperty("net.sf.jasperreports.compiler.temp.dir", tempFolder.getAbsolutePath())
            }

            if (!tempFolder.exists()) {
                if (!tempFolder.mkdirs()) {
                    throw new Exception("Unable to create temp folder: ${tempFolder.getPath()}")
                }
            }
        }
    }

    /**
     * Generate a exporter with for a JasperReportDef. Note that SUBREPORT_DIR an locale have default
     * values.
     * @param reportDef
     * @return Exporter
     */
    private Exporter generateExporter(JasperReportDef reportDef) {
        if (reportDef.parameters.SUBREPORT_DIR == null) {
            reportDef.parameters.SUBREPORT_DIR = reportDef.getFilePath()
        }

        if (reportDef.parameters.locale) {
            if (reportDef.parameters.locale instanceof String) {
                reportDef.parameters.REPORT_LOCALE = getLocaleFromString(reportDef.parameters.locale)
            } else if (reportDef.parameters.locale instanceof Locale) {
                reportDef.parameters.REPORT_LOCALE = reportDef.parameters.locale
            }
        } else if (reportDef.locale) {
            reportDef.parameters.REPORT_LOCALE = reportDef.locale
        } else {
            reportDef.parameters.REPORT_LOCALE = Locale.getDefault()
        }

        def exporter = JasperExportFormat.getExporter(reportDef.fileFormat)
        return exporter
    }

    private ReportExportConfiguration getExportReportConfiguration(JasperReportDef reportDef){

        List<MetaProperty> props = JasperExportFormat.getReportConfigurationProperties(reportDef.fileFormat)
        def reportConfig = JasperExportFormat.getReportConfiguration(reportDef.fileFormat)
        Boolean useDefaultParameters = reportDef.parameters.useDefaultParameters.equals("true")
        if (useDefaultParameters) {
            applyDefaultReportExportConfiguration(reportConfig, reportDef.fileFormat)
        }

        if (props) {
            applyCustomParameters(props, reportConfig, reportDef.parameters)
        }

        return reportConfig
    }

    private ExporterConfiguration getExporterConfiguration(JasperReportDef reportDef){

        List<MetaProperty> props = JasperExportFormat.getExporterProperties(reportDef.fileFormat)
        def expConfig = JasperExportFormat.getExporterConfiguration(reportDef.fileFormat)

        if (props) {
            applyCustomParameters(props, expConfig, reportDef.parameters)
        }

        return expConfig
    }

    /**
     * Generate a JasperPrint object for a given report.
     * @param reportDefinition , the report
     * @param parameters , additional parameters
     * @return JasperPrint , jasperreport printer
     */
    private JasperPrint generatePrinter(JasperReportDef reportDef) {
        JasperPrint jasperPrint
        Resource resource = reportDef.getReport()
        JRDataSource jrDataSource = reportDef.dataSource

        if (jrDataSource == null && reportDef.reportData != null && !reportDef.reportData.isEmpty()) {
            jrDataSource = new JRBeanCollectionDataSource(reportDef.reportData)
        }

        if (jrDataSource != null) {
            if (resource.getFilename().endsWith('.jasper')) {
                jasperPrint = JasperFillManager.fillReport(resource.inputStream, reportDef.parameters, (JRDataSource)jrDataSource)
            }
            else {
                forceTempFolder()
                jasperPrint = JasperFillManager.fillReport(JasperCompileManager.compileReport(resource.inputStream), reportDef.parameters,
                        (JRDataSource)jrDataSource)
            }
        }
        else {

            Sql sql = dataSource ? new Sql(dataSource) : null
            Connection connection = dataSource?.getConnection()

            try {
                if (resource.getFilename().endsWith('.jasper')) {
                    jasperPrint = JasperFillManager.fillReport(resource.inputStream, reportDef.parameters, (Connection)connection)
                }
                else {
                    forceTempFolder()
                    jasperPrint = JasperFillManager.fillReport(JasperCompileManager.compileReport(resource.inputStream), reportDef.parameters,
                            (Connection)connection)
                }
            }
            finally {
                sql?.close()
                connection?.close()
            }
        }

        return jasperPrint
    }

    /**
     * Apply configuration values.
     * If the user submits a parameter that is not available for the file format this parameter is ignored.
     * @param properties , available properties for the chosen file format
     * @param exporter , the exporter object
     * @param parameter , the parameters to apply
     */
    private void applyCustomParameters(List<MetaProperty> properties, CommonExportConfiguration configuration, Map<String, Object> parameters) {
        def propertyNames = properties.collect {it.name}

        parameters.each { p ->
            def propName = p.getKey()
            if (propertyNames.contains(propName)) {
                def prop = configuration.hasProperty(propName)
                if(prop){
                    def val = p.getValue()
                    if(prop.type.isAssignableFrom(val?.getClass())) {
                        configuration."${propName}" = val
                    }
                }
            }
        }
    }

    /**
     * Apply the default ReportExportConfiguration for a bunch of file formats and only if useDefaultParameters is enabled.
     * @param exporter , the Exporter
     * @param format , the target file format
     */
    private void applyDefaultReportExportConfiguration(ReportExportConfiguration reportExportConfiguration, JasperExportFormat format) {
        switch (format) {
            case JasperExportFormat.HTML_FORMAT:
                reportExportConfiguration.useBackgroundImageToAlign = false
            break
            case JasperExportFormat.XLS_FORMAT:
                reportExportConfiguration.onePagePerSheet = true
                reportExportConfiguration.whitePageBackground = false
                reportExportConfiguration.removeEmptySpaceBetweenRows = true
                reportExportConfiguration.detectCellType = true
            break
            case JasperExportFormat.TEXT_FORMAT:
                reportExportConfiguration.pageWidthInChars = 80
                reportExportConfiguration.pageHightInChars = 60
            break
        }
    }

    /**
     * Convert a String to a Locale.
     * @param localeString , a string
     * @returns Locale
     */
    static Locale getLocaleFromString(String localeString) {
        if (localeString == null) {
            return null
        }
        localeString = localeString.trim()

        // Extract language
        int languageIndex = localeString.indexOf('_')
        String language
        if (languageIndex == -1) {  // No further "_" so is "{language}" only
            return new Locale(localeString, "")
        }
        language = localeString.substring(0, languageIndex)

        // Extract country
        int countryIndex = localeString.indexOf('_', languageIndex + 1)
        String country
        if (countryIndex == -1) {     // No further "_" so is "{language}_{country}"
            country = localeString.substring(languageIndex + 1)
            return new Locale(language, country)
        }
        // Assume all remaining is the variant so is "{language}_{country}_{variant}"
        country = localeString.substring(languageIndex + 1, countryIndex)
        String variant = localeString.substring(countryIndex + 1)
        return new Locale(language, country, variant)
    }
}
