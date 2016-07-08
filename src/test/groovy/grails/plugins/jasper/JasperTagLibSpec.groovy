package grails.plugins.jasper
/* Copyright 2006-2009 the original author or authors.
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


import grails.test.mixin.TestFor
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.grails.web.util.WebUtils
import org.springframework.web.context.request.RequestAttributes
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

/**
 * @author Puneet Behl
 */
@ConfineMetaClassChanges(String)
@TestFor(JasperTagLib)
class JasperTagLibSpec extends Specification {

	static final String SCRIPT_PART = """<script type="text/javascript"> function submit_myreport(link) { link.parentNode._format.value = link.title;
          link.parentNode.submit(); return false; } </script> """

	void setup() {
		WebUtils.retrieveGrailsWebRequest().setAttribute(WebUtils.INCLUDE_CONTEXT_PATH_ATTRIBUTE, "/myapp", RequestAttributes.SCOPE_REQUEST)
		String.metaClass.squeezeWhitespace { delegate.replaceAll(/\n/, ' ').replaceAll(/ {2,}/, ' ').replaceAll(/^ /, '').replaceAll(/ $/, '')}
	}
	
    void "Tag 'jasperReport' attribute format is required"() {
        when:
        applyTemplate('<g:jasperReport jasper="myreport"/>')
		
		then:
		thrown(Exception)
    }
	
	void "Tag 'jasperReport' attribute jasper is required"() {
		when:
		applyTemplate('<g:jasperReport format="pdf"/>')
		
		then:
		thrown(Exception)
	}
	
	void "call using all default values, should result in a single, plain link enclosed in vert-bar delimiters"() {
		expect:
		applyTemplate('<g:jasperReport format="pdf" jasper="myreport"/>').squeezeWhitespace() == 
			"""| <a class="jasperButton" title="PDF" href="/myapp/jasper?_format=PDF&_name=&_file=myreport">
        <img border="0" alt="PDF" src="/images/icons/PDF.gif" /></a> | """.squeezeWhitespace()	
	}
	
	void "call using all default values, should result in a single, plain link, but with delimiters suppressed"() {
		expect:
		applyTemplate('<g:jasperReport format="pdf" jasper="myreport" name="The Report" delimiter=" "/>').squeezeWhitespace() == 
		"""<a class="jasperButton" title="PDF" href="/myapp/jasper?_format=PDF&_name=The+Report&_file=myreport">
                <img border="0" alt="PDF" src="/images/icons/PDF.gif" /></a>  <strong>The Report</strong>""".squeezeWhitespace()
	}
	
	void "should result in dual links with delimiters suppressed"() {
		expect:
		applyTemplate('<g:jasperReport format="pdf, rtf" jasper="myreport" name="The Report" delimiter=" "/>').squeezeWhitespace() ==
		"""<a class="jasperButton" title="PDF" href="/myapp/jasper?_format=PDF&_name=The+Report&_file=myreport">
                <img border="0" alt="PDF" src="/images/icons/PDF.gif" /></a>  <a class="jasperButton" title="RTF" href="/myapp/jasper?_format=RTF&_name=The+Report&_file=myreport">
                <img border="0" alt="RTF" src="/images/icons/RTF.gif" /></a>  <strong>The Report</strong>""".squeezeWhitespace()
	}
	
	void "a minimal call with a report name"() {
		expect:
		applyTemplate('<g:jasperReport format="pdf" jasper="myreport" name="Print as PDF"/>').squeezeWhitespace() == 
		"""|
                <a class="jasperButton" title="PDF" href="/myapp/jasper?_format=PDF&_name=Print+as+PDF&_file=myreport">
                <img border="0" alt="PDF" src="/images/icons/PDF.gif" /></a> | <strong>Print as PDF</strong>""".squeezeWhitespace()
	}
	
	
	void "a minimal call with description"() {
		expect:
		applyTemplate('<g:jasperReport format="pdf" jasper="myreport" description="Print as PDF"/>').squeezeWhitespace() == 
		"""|
                <a class="jasperButton" title="PDF" href="/myapp/jasper?_format=PDF&_name=&_file=myreport">
                <img border="0" alt="PDF" src="/images/icons/PDF.gif" /></a> | Print as PDF""".squeezeWhitespace()
	}
	
	void "a call with a body, thus resulting in a form"() {
		expect:
		applyTemplate('<g:jasperReport format="pdf" jasper="myreport">A Body</g:jasperReport>').squeezeWhitespace() ==
		(SCRIPT_PART + """
                <form class="jasperReport" name="myreport" action="/myapp/jasper"><input type="hidden" name="_format" />
                  <input type="hidden" name="_name" value="" />
                  <input type="hidden" name="_file" value="myreport" />
                | <a href="#" class="jasperButton" title="PDF" onclick="return submit_myreport(this)">
                <img border="0" alt="PDF" src="/images/icons/PDF.gif" /></a> |&nbsp;A Body</form>
                """).squeezeWhitespace()
	}
}
