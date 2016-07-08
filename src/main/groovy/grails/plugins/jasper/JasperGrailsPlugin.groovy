package grails.plugins.jasper

import grails.plugins.Plugin
import net.sf.jasperreports.j2ee.servlets.ImageServlet
import org.springframework.boot.context.embedded.ServletRegistrationBean

class JasperGrailsPlugin extends Plugin {
    def grailsVersion = "3.0.2 > *"
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            'docs/**'
    ]

    def title = "Jasper Plugin"
    def author = "Craig Andrews"
    def authorEmail = "candrews@integralblue.com"
    def description = '''
    Adds easy support for launching jasper reports from GSP pages.
    After installing, run your application and request (app-url)/jasper/demo for a demonstration and instructions.
    '''
    def profiles = ['web']
    def documentation = "http://www.grails.org/plugin/jasper"
    def license = "APACHE"
	def developers = [	[ name: "Burt Beckwith", email: "burt@burtbeckwith.com" ], 
					  	[ name: "Puneet Behl", email: "puneet.behl007@gmail.com" ],
					   	[ name: "Mansi Arora", email: "mansi.arora@tothenew.com" ],
					   	[ name: "Manvendra Singh", email: "manvendrask@live.com" ],
					]
    def issueManagement = [system: "JIRA", url: "https://github.com/puneetbehl/grails-jasper/issues"]
    def scm = [url: "https://github.com/puneetbehl/grails-jasper"]

    Closure doWithSpring() {
        { ->
            imageServlet(ImageServlet)
            dispatchServletRegistrationBean(ServletRegistrationBean) {
                servlet = ref(imageServlet)
                urlMappings = ["/reports/image"]
            }
        }
    }
}
