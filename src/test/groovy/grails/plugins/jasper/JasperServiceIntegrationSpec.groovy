package grails.plugins.jasper

import grails.testing.mixin.integration.Integration
import net.sf.jasperreports.export.SimplePdfExporterConfiguration
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import grails.plugins.jasper.demo.ExamplePersonForReport

@Integration
class JasperServiceIntegrationSpec extends Specification {
    @Autowired
    JasperService jasperService

    void "Generate PDF report with custom configurations"(){
        when:
        def jasperReportDefParameters =  [:]
        def model = [:]
        def user = System.getProperty("user.name")
        model.data = [new ExamplePersonForReport(name: 'Amy', email: 'amy@example.com'),
                new ExamplePersonForReport(name: 'Brad', email: 'brad@example.com'),
                new ExamplePersonForReport(name: 'Charlie', email: 'charlie@example.com')]
        jasperReportDefParameters._format = "PDF"
        jasperReportDefParameters._file = "w_iReport"
        jasperReportDefParameters.metadataAuthor = user
        JasperReportDef reportDef = jasperService.buildReportDefinition(jasperReportDefParameters, Locale.ENGLISH, model)

        then:
        reportDef.contentStream != null
        reportDef.fileFormat == JasperExportFormat.PDF_FORMAT
        reportDef.exporterConfiguration instanceof SimplePdfExporterConfiguration
        reportDef.exporterConfiguration.metadataAuthor == user
    }
}
