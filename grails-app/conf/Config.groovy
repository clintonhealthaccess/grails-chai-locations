// configuration for plugin testing - will not be included in the plugin zip
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'

	debug  'groovyx.net'
	debug  'grails.app'
}

i18nFields{
	locales = ["en","fr","rw"]
	extraLocales = ["rw"]
}

rabbitmq {
	connectionfactory {
		username = 'guest'
		password = 'guest'
		hostname = '127.0.0.1'
	}
}

sync.type.mapping = [
	"CS": "Health Center",
	"DH": "District Hospital"
]
sync.date.format = "EEE, d MMM yyyy HH:mm:ss Z"