package grails.plugins.jasper

import grails.plugins.*
import net.sf.jasperreports.j2ee.servlets.ImageServlet
import org.springframework.boot.web.servlet.ServletRegistrationBean

class JasperGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "5.1.9 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "**/grails/plugins/jasper/demo/**",
        "grails-app/views/jasperDemo/demo.gsp",
        "grails-app/views/error.gsp",
        "public/reports/**",
        "**/test/**",
        "docs/**"
    ]

    def title = "Jasper Plugin"
    def author = "Craig Andrews"
    def authorEmail = "candrews@integralblue.com"
    def description = '''
    Adds easy support for launching JasperReports reports from GSP pages.
    '''
    def profiles = ['web']

    def documentation = "http://www.grails.org/plugin/jasper"
    def license = "APACHE"
    def developers = [
            [ name: "Burt Beckwith", email: "burt@burtbeckwith.com" ],
            [ name: "Puneet Behl", email: "puneet.behl007@gmail.com" ],
            [ name: "Mansi Arora", email: "mansi.arora@tothenew.com" ],
            [ name: "Manvendra Singh", email: "manvendrask@live.com" ],
            [ name: "Daraii", email: "daraii@jellycat.io" ]
    ]
    def issueManagement = [system: "GITHUB", url: "https://github.com/daraii/grails-jasper/issues"]
    def scm = [url: "https://github.com/daraii/grails-jasper"]

    Closure doWithSpring() { {->
        imageServlet(ImageServlet)
        dispatchServletRegistrationBean(ServletRegistrationBean) {
            servlet = ref('imageServlet')
            urlMappings = ["/reports/image"]
        }
    } }
}
