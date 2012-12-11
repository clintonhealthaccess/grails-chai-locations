package org.chai.location

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

import groovy.util.XmlSlurper

class SyncServiceSpec extends IntegrationTests {
	
	def syncService
	
	/* def "test sync from feed"() {
		when:
		syncService.syncFromActivityFeed() 
		
		then:
		DataLocation.count() > 0
	}
	
	def "test sync from full list"() {
		when:
		syncService.syncFromFullList()
		
		then:
		DataLocation.count() > 0
	} */
		
	def "test add site when associated location does not exist"() {
		setup:
		def xml = prepareXML('site', 'created', 100000)
		syncService.metaClass.getSite = {id -> 
			[properties: [sectorCode: 'not_existant', type: "CS", fosaid: "code"]]
		}
		
		when:
		syncService.syncFromActivityXML(xml, new Date())
		
		then: // TODO
		DataLocation.count() == 1
		DataLocation.findByCode('code').type.equals (DataLocationType.findByCode(HEALTH_CENTER_GROUP)) 
		DataLocation.findByCode('code').location == null
		SyncChange.count() == 1
		SyncChange.list()[0].dataLocation.equals DataLocation.findByCode('code')
		SyncChange.list()[0].needsReview == true
	}
	
	def "test add site when associated type does not exist"() {
		setup:
		setupLocationTree()
		def xml = prepareXML('site', 'created', 100000)
		syncService.metaClass.getSite = {id -> 
			[
			name: "Dummy", 
			properties: [(SyncService.PROPERTY_PARENT): IntegrationTests.BURERA, type: "non_existant", fosaid: "code"]
			]
		}
		
		when:
		syncService.syncFromActivityXML(xml, new Date())
		
		then: // TODO
		DataLocation.count() == 3
		DataLocation.findByCode('code').location.equals (Location.findByCode(BURERA)) 
		DataLocation.findByCode('code').type == null
		SyncChange.count() == 1
		SyncChange.list()[0].dataLocation.equals DataLocation.findByCode('code')
		SyncChange.list()[0].needsReview == true
	}

	def "test add site when associated location and type exists"() {
		setup:
		setupLocationTree()
		def xml = prepareXML('site', 'created', 100000)
		syncService.metaClass.getSite = {id -> 
			[
			name: "Dummy", 
			properties: [(SyncService.PROPERTY_PARENT): IntegrationTests.BURERA, type: "CS", fosaid: "code"]
			]
		}
		
		when:
		syncService.syncFromActivityXML(xml, new Date())
		
		then: // TODO
		DataLocation.count() == 3
		DataLocation.findByCode('code').type.equals (DataLocationType.findByCode(HEALTH_CENTER_GROUP)) 
		DataLocation.findByCode('code').location.equals (Location.findByCode(BURERA)) 
		SyncChange.count() == 1
		SyncChange.list()[0].dataLocation.equals DataLocation.findByCode('code')
		SyncChange.list()[0].needsReview == false
	}
	
	def "test add site when type is ignored"() {
		setup:
		setupLocationTree()
		def xml = prepareXML('site', 'created', 100000)
		syncService.metaClass.getSite = {id -> 
			[
			name: "Dummy", 
			properties: [(SyncService.PROPERTY_PARENT): IntegrationTests.BURERA, type: "MU", fosaid: "code"]
			]
		}
		syncService.metaClass.getIgnoredTypes = {return ['MU']}
		
		when:
		syncService.syncFromActivityXML(xml, new Date())
		
		then: // TODO
		DataLocation.count() == 2
		SyncChange.count() == 0
	}
	
	def "test add site when site already exists"() {
		setup:
		setupLocationTree()
		def xml = prepareXML('site', 'created', 100000)
		syncService.metaClass.getSite = {id -> 
			[
			name: "Dummy", 
			properties: [(SyncService.PROPERTY_PARENT): IntegrationTests.BURERA, type: "CS", fosaid: IntegrationTests.BUTARO]
			]
		}
		
		when:
		syncService.syncFromActivityXML(xml, new Date())
		
		then: // TODO
		DataLocation.count() == 2
		SyncChange.count() == 1
		SyncChange.list()[0].dataLocation.equals DataLocation.findByCode(IntegrationTests.BUTARO)
		SyncChange.list()[0].needsReview == true
	}
	
	def "test update site when location does not exist"() {
		setup:
		setupLocationTree()
		def xml = prepareXML('site', 'changed', DataLocation.findByCode(BUTARO).id)
		syncService.metaClass.getSite = {id -> 
			[properties: [fosaid: 'code', (SyncService.PROPERTY_PARENT): IntegrationTests.BURERA, type: "CS"]]
		}
		
		when:
		syncService.syncFromActivityXML(xml, new Date())
		
		then: // TODO
		DataLocation.count() == 3
		// type is not changed
		DataLocation.findByCode('code').type.equals DataLocationType.findByCode(HEALTH_CENTER_GROUP)
		SyncChange.count() == 1
		SyncChange.list()[0].dataLocation.equals DataLocation.findByCode('code')
		SyncChange.list()[0].needsReview == true
	}
	
	def "test update site change associated location does not exist"() {
		setup:
		setupLocationTree()
		def xml = prepareXML('site', 'changed', DataLocation.findByCode(BUTARO).id)
		syncService.metaClass.getSite = {id -> 
			[properties: [(SyncService.PROPERTY_PARENT): 'not_existant', fosaid: IntegrationTests.BUTARO]]
		}
		
		when:
		syncService.syncFromActivityXML(xml, new Date())
		
		then: // TODO
		DataLocation.count() == 2
		// location is not changed
		DataLocation.findByCode(BUTARO).location.equals Location.findByCode(BURERA)
		SyncChange.count() == 1
		SyncChange.list()[0].dataLocation.equals DataLocation.findByCode(IntegrationTests.BUTARO)
		SyncChange.list()[0].needsReview == true
	}
	
	def "test update site change associated type does not exist"() {
		setup:
		setupLocationTree()
		def xml = prepareXML('site', 'changed', DataLocation.findByCode(BUTARO).id)
		syncService.metaClass.getSite = {id -> 
			[properties: [type: 'not_existant', fosaid: IntegrationTests.BUTARO]]
		}
		
		when:
		syncService.syncFromActivityXML(xml, new Date())
		
		then: // TODO
		DataLocation.count() == 2
		// type is not changed
		DataLocation.findByCode(BUTARO).type.equals DataLocationType.findByCode(DISTRICT_HOSPITAL_GROUP)
		SyncChange.count() == 1
		SyncChange.list()[0].dataLocation.equals DataLocation.findByCode(IntegrationTests.BUTARO)
		SyncChange.list()[0].needsReview == true
	}
	
	def "test update site change associated location exists"() {
		setup:
		setupLocationTree()
		def xml = prepareXML('site', 'changed', DataLocation.findByCode(BUTARO).id)
		syncService.metaClass.getSite = {id -> 
			[properties: [(SyncService.PROPERTY_PARENT): IntegrationTests.NORTH, type: 'HD', fosaid: IntegrationTests.BUTARO]]
		}
		
		when:
		syncService.syncFromActivityXML(xml, new Date())
		
		then: // TODO
		DataLocation.count() == 2
		// location does not change
		DataLocation.findByCode(BUTARO).location.equals Location.findByCode(BURERA)
		SyncChange.count() == 1
		SyncChange.list()[0].dataLocation.equals DataLocation.findByCode(IntegrationTests.BUTARO)
		SyncChange.list()[0].needsReview == true
	}
	
	def "test update site change associated type exists"() {
		setup:
		setupLocationTree()
		def xml = prepareXML('site', 'changed', DataLocation.findByCode(BUTARO).id)
		syncService.metaClass.getSite = {id -> 
			[properties: [(SyncService.PROPERTY_PARENT): IntegrationTests.BURERA, type: 'CS', fosaid: IntegrationTests.BUTARO]]
		}
		
		when:
		syncService.syncFromActivityXML(xml, new Date())
		
		then: // TODO
		DataLocation.count() == 2
		// type is not changed
		DataLocation.findByCode(BUTARO).type.equals DataLocationType.findByCode(DISTRICT_HOSPITAL_GROUP)
		SyncChange.count() == 1
		SyncChange.list()[0].dataLocation.equals DataLocation.findByCode(IntegrationTests.BUTARO)
		SyncChange.list()[0].needsReview == true
	}
	
	def "test delete site"() {
		setup:
		setupLocationTree()
		def dataLocation = DataLocation.findByCode(BUTARO)
		dataLocation.itemid = 123
		dataLocation.save()
		def xml = prepareXML('site', 'deleted', 123)
		
		when:
		syncService.syncFromActivityXML(xml, new Date())
		
		then: // TODO
		DataLocation.count() == 2
		// it is not deleted
		DataLocation.findByCode(BUTARO).code == BUTARO
		SyncChange.count() == 1
		SyncChange.list()[0].dataLocation.equals DataLocation.findByCode(IntegrationTests.BUTARO)
		SyncChange.list()[0].needsReview == true
	}
	
	def "test delete inexisting site"() {
		setup:
		setupLocationTree()
		def xml = prepareXML('site', 'deleted', 0)
		
		when:
		syncService.syncFromActivityXML(xml, new Date())
		
		then: // TODO
		DataLocation.count() == 2
		// it is not deleted
		DataLocation.findByCode(BUTARO).code == BUTARO
		SyncChange.count() == 0
	}
	
	def "test change after date"() {
		setup:
		setupLocationTree()
		def xml = prepareXML('site', 'created', 100000)
		syncService.metaClass.getSite = {id -> 
			[
			name: "Dummy", 
			properties: [(SyncService.PROPERTY_PARENT): IntegrationTests.BURERA, type: "CS", fosaid: "code"]
			]
		}
		
		when:
		syncService.syncFromActivityXML(xml, new Date() + 1)
		
		then: // TODO
		DataLocation.count() == 2
		SyncChange.count() == 0
	}
	
	def prepareXML(def itemtype, def action, def id) {
		return new XmlSlurper().parseText(
			'<?xml version="1.0" encoding="UTF-8"?>' +
			'<rss version="2.0" xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#" xmlns:rm="http://resourcemap.instedd.org/api/1.0" xmlns:atom="http://www.w3.org/2005/Atom">' +
		  	'	<channel>' +
		    '		<title>Activity</title>' +
		    '		<lastBuildDate>Wed, 24 Oct 2012 10:55:28 -0500</lastBuildDate> ' +
		    '		<atom:link rel="next" href="http://facilities.moh.gov.rw/api/activity.rss?collection_ids%5B%5D=26&amp;page=2"/>"' +
			'		<item>' +
			'		  <title>Test</title>' +
			'		  <pubDate>'+ syncService.dateFormat.format(new Date() + 1)+'</pubDate>' +
			'		  <guid>4734</guid>' +
			'		  <rm:collection>Rwanda Health Facility Registry</rm:collection>' +
			"		  <rm:itemtype>${itemtype}</rm:itemtype>" +
			"		  <rm:itemid>${id}</rm:itemid>" +
			"		  <rm:action>${action}</rm:action>" +
			'		  <rm:user>edjez@instedd.org</rm:user>' +
			'		</item>' +
		  	'	</channel>' +
			'</rss>')
	}
	
}