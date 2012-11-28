/*
 * Copyright (c) 2012, Clinton Health Access Initiative.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Clinton Health Access Initiative nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CLINTON HEALTH ACCESS INITIATIVE BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

class ChaiLocationsGrailsPlugin {
	
    def version = "0.5-CHAI"
    def grailsVersion = "1.3.7 > *"
    def dependsOn = [:]
    def pluginExcludes = [
        "grails-app/views/error.gsp",
		"grails-app/views/*",
		"web-app/css/*",
		"web-app/js/*",
		"web-app/images/*"
    ]

    def title = "CHAI Locations Plugin" // Headline display name of the plugin
    def author = "Eugene Munyaneza"
    def authorEmail = "emunyaneza@clintonhealthaccess.org"
    def description = '''\
CHAI locations plugin.
'''

    def documentation = "http://github.org/uginm102/grails-chai-locations"

    def license = "BSD3"
    def organization = [ name: "Clinton Health Access Initiative", url: "http://www.clintonhealthaccess.org/" ]
    def developers = [ 
		[ name: "Jean Kahigiso", email: "jkahigiso@clintonhealthaccess.org" ],
		[ name: "Sue Lister", email: "slister@clintonhealthaccess.org" ],
		[ name: "François Terrier", email: "fterrier@clintonhealthaccess.org" ]
	]

    def scm = [ url: "http://github.com/uginm102/grails-chai-locations" ]

}
