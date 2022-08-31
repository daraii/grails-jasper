package grails.plugins.jasper

import grails.testing.mixin.integration.Integration
import grails.util.Holders
import org.springframework.core.io.Resource
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

import static grails.plugins.jasper.JasperPluginUtils.TEMP_COMPILE_SUBDIRECTORY

@Integration
class JasperReportDefIntegrationSpec extends Specification {
    void "Get sample report from classpath using the default location"() {
        when:
        def report = new JasperReportDef(name: "sample-jasper-plugin")

        then:
        report.getReport().exists()
    }

    void "Set report path"() {
        when:
        def directory = "classpath:/public/reports/"
        def file = "sample-jasper-plugin"
        def ext = "jrxml"
        def report = new JasperReportDef()
        report.setFilePath(directory + file + "." + ext)

        then:
        report.folder == directory
        report.name == file
    }

    void "Get sample report with configured directory"() {
        when:
        grails.util.Holders.grailsApplication.config.put(JasperPluginUtils.REPORT_DIRECTORY_PROP, "classpath:/public/test/reports/")
        def report = new JasperReportDef(name: "sample-jasper-plugin")

        then:
        report.getReport().exists()

        cleanup:
        grails.util.Holders.grailsApplication.config.remove(JasperPluginUtils.REPORT_DIRECTORY_PROP)
    }

    void "Get sample report with file path"() {
        when:
        String userHomeDir = System.getProperty('user.home')
        File tempFolder = new File(userHomeDir, TEMP_COMPILE_SUBDIRECTORY)
        def fileName = "sample-jasper-plugin.jrxml"
        if (!tempFolder.exists()) {
            tempFolder.mkdirs()
        }
        def sampleReportPath = "classpath:/public/test/reports/sample-jasper-plugin.jrxml"
        def sampleReportResource = Holders.grailsApplication.mainContext.getResource(sampleReportPath)
        def newSampleReportFile = new File(userHomeDir, TEMP_COMPILE_SUBDIRECTORY + File.separator + fileName)
        Files.copy(Paths.get(sampleReportResource.getURI()), Paths.get(newSampleReportFile.toURI()), StandardCopyOption.REPLACE_EXISTING)

        def directoryPath = "file:" + userHomeDir + TEMP_COMPILE_SUBDIRECTORY
        def report = new JasperReportDef(name: "sample-jasper-plugin", folder: directoryPath)

        then:
        report.getReport().exists()

        cleanup:
        doCleanup(newSampleReportFile)
    }

    private void doCleanup(report) {
        if (report) {
            if (report.exists()) {
                if (report instanceof Resource) {
                    report.getFile().delete()
                }
                else if(report instanceof File){
                    report.delete()
                }
            }
        }
    }
}
