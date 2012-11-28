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

import groovyx.net.http.Method
import groovyx.net.http.HTTPBuilder

import static groovyx.net.http.ContentType.XML
import static groovyx.net.http.ContentType.JSON

import java.util.ArrayList
import java.util.HashMap
import java.text.SimpleDateFormat

class SyncService {

	static final ITEM_TYPE_SITE = 'site'
	static final ITEM_TYPE_LAYER = 'layer'

	static final ACTION_CHANGED = 'changed'
	static final ACTION_DELETED = 'deleted'
	static final ACTION_CREATED = 'created'

	static final PROPERTY_CODE = 'fosaid'
	static final PROPERTY_TYPE = 'type'

	static final PROPERTY_PARENT = 'adminunit'

	def grailsApplication

	def getActivityFeedURL() {
		return grailsApplication.config.sync.activity.feed.url
	}

	def getFullListURL() {
		return grailsApplication.config.sync.full.list.url
	}

	def getSiteURL() {
		return grailsApplication.config.sync.site.url
	}
	
	def getSiteUsername() {
		return grailsApplication.config.sync.site.username
	}
	
	def getSitePassword() {
		return grailsApplication.config.sync.site.password
	}
	
	def getLocationTypeMapping() {
		return new HashMap(grailsApplication.config.sync.type.mapping)
	}
	
	def getIgnoredTypes() {
		return new ArrayList(grailsApplication.config.sync.type.ignore)
	}
	
	private SimpleDateFormat cachedDateFormat
	
	def getDateFormat() {
		if (cachedDateFormat == null) {
			cachedDateFormat = new SimpleDateFormat(grailsApplication.config.sync.date.format)
		}
		return cachedDateFormat
	}

 	def syncFromActivityFeed(def date) {
		log.debug("getting activity feed from URL: ${activityFeedURL}")
		def http = new HTTPBuilder(activityFeedURL)
		http.auth.basic siteUsername, sitePassword

		http.request(Method.GET, XML) {
			response.success = { resp, xml ->
				log.debug("succesfully retrieved activity feed, title: ${xml.channel.title}")
				syncFromXML(xml, date)
			}
		}
	}
	
	def syncFromFullList() {
		log.debug("getting full list from URL: ${fullListURL}")

		def url = fullListURL
		
		def sitesSeen = []
		while (url != null) {
			def json = getFullListPage(url)
			
			json.sites.each { site ->
				log.debug("syncing site: ${site.id}, code: ${site.properties[PROPERTY_CODE]}")
				sitesSeen << site.id + ''
				
				DataLocation location = DataLocation.findByCode(site.properties[PROPERTY_CODE])
				if (location != null) {
					// we just update the site
					log.debug("site found, updating")
					changeSite(site)
				}
				else {
					// we create a new site
					log.debug("site not found, creating")
					createSite(site)
				}				
			}
			url = json.nextPage
		}
		
		log.debug("found sites: ${sitesSeen}")
		DataLocation.list().each { location ->
			if (location.itemid == null || !sitesSeen.contains(location.itemid + '')) {
				// we delete in that case
				log.debug("site with itemid ${location.itemid} was not in full list, deleting")
				deleteDataLocation(location)
			}
		}
	}
	
	def getFullListPage(def url) {
		def http = new HTTPBuilder(url)
		http.auth.basic siteUsername, sitePassword
		
		log.debug("retrieving page: ${url}")
		http.request(Method.GET, JSON) {
			response.success = { resp, json ->
				log.debug("succesfully retrieved ${json.sites.size()} sites")
				return json
			}
		}
	}
	
	def syncFromActivityXML(def xml, def date) {
		def sitesCreated = []
		def sitesDeleted = []
		def sitesChanged = []
		
		xml.channel.item.each { item ->
			log.debug("processing item: ${item}")
			
			def pubDate = getDateFormat().parse(item.pubDate.toString())
			if (pubDate.after(date)) handleItem(item, sitesCreated, sitesDeleted, sitesChanged)
			else log.debug("item is published before ${date}, skipping")
		}
		
		log.debug("found ${sitesCreated.size()} created sites, ${sitesDeleted.size()} deleted sites and ${sitesChanged.size()} changed sites")
		
		sitesCreated.each {createSite(getSite(it))}
		sitesChanged.each {changeSite(getSite(it))}
		sitesDeleted.each {deleteSite(it)}
	}

	def handleItem(def item, def sitesCreated, def sitesDeleted, def sitesChanged) {
		log.debug("handling item with title: ${item.title}, and type: ${item.itemtype}, and action: ${item.action}")
				
		switch(item.itemtype) {
			case ITEM_TYPE_SITE:
				switch(item.action) {
					case ACTION_CREATED:
						sitesCreated << item.itemid
					break
					case ACTION_DELETED:
						sitesDeleted << item.itemid
					break
					case ACTION_CHANGED:
						sitesChanged << item.itemid
					break
				}
			break	
			case ITEM_TYPE_LAYER:
				// TODO
			break
		}
	}
	
	def getSite(def siteId) {
		log.debug("getting site information from URL: ${siteURL}")
		def http = new HTTPBuilder(siteURL.replace('\${itemid}', siteId))
		http.auth.basic siteUsername, sitePassword

		http.request(Method.GET, JSON) {
			response.success = { resp, json ->
				log.debug("succesfully retrieved site information: ${json}")
				return json
			}
		}
	}
 
	def createSite(def site) {
		log.debug("creating site with code: ${site.properties[PROPERTY_CODE]}, type: ${site.properties[PROPERTY_TYPE]}, parent: ${site.properties[PROPERTY_PARENT]}")
		
		if (ignoredTypes.contains(site.properties[PROPERTY_TYPE])) {
			log.debug("type ${site.properties[PROPERTY_TYPE]} is in ignore list, ignoring")
		}
		else {
			DataLocation dataLocation = DataLocation.findByCode(site.properties[PROPERTY_CODE])
			SyncChange change = new SyncChange(needsReview: false)
		
			
			if (dataLocation != null) {
				change.needsReview = true
				change.addToMessages "Data location already exists: ${site.properties[PROPERTY_CODE]}"
			}
			else {
		 		dataLocation = new DataLocation(code: site.properties[PROPERTY_CODE])
				change.addToMessages "Created site ${site.name}, code: ${dataLocation.code}"
			}
		
			DataLocationType type = DataLocationType.findByCode(getLocationTypeMapping().get(site.properties[PROPERTY_TYPE])?:'')
			Location parent = Location.findByCode(site.properties[PROPERTY_PARENT]?:'')

			if (type != null) type.addToDataLocations(dataLocation)
			if (parent != null) parent.addToDataLocations(dataLocation)
	
			grailsApplication.config.i18nFields.locales.each{ loc ->
				dataLocation.setNames(site.name, new Locale(loc))
			}

			checkTypeAndParent(type, parent, change, site)
			
			dataLocation.addToChanges(change)
			dataLocation.needsReview = change.needsReview
			dataLocation.itemid = site.id
			dataLocation.save(validate: false)
		}
	}
	
	private def checkTypeAndParent(def type, def parent, def change, def site) {
		if (type == null || parent == null) {
			change.needsReview = true
			if (type == null) {
				if (ignoredTypes.contains(site.properties[PROPERTY_TYPE])) {
					change.addToMessages "Changing to ignored type: ${site.properties[PROPERTY_TYPE]}"
				}
				else {
					change.addToMessages "Type not found: ${site.properties[PROPERTY_TYPE]}"
				}
			}
			if (parent == null) {
				change.addToMessages "Parent not found: ${site.properties[PROPERTY_PARENT]}"
			}
			return false
		}
		return true
	}
	
	def changeSite(def site) {
		log.debug("changing site with code: ${site.properties[PROPERTY_CODE]}, type: ${site.properties[PROPERTY_TYPE]}, parent: ${site.properties[PROPERTY_PARENT]}")
		
		DataLocation dataLocation = DataLocation.findByCode(site.properties[PROPERTY_CODE])
		SyncChange change = new SyncChange(needsReview: false)
		DataLocationType type = DataLocationType.findByCode(getLocationTypeMapping().get(site.properties[PROPERTY_TYPE])?:'')
		Location parent = Location.findByCode(site.properties[PROPERTY_PARENT]?:'')
		
		if (dataLocation == null) {
			change.needsReview = true
			change.addToMessages "Data location does not exist: ${site.properties[PROPERTY_CODE]}"
			dataLocation = new DataLocation(code: site.properties[PROPERTY_CODE])
			
			if (type != null) type.addToDataLocations(dataLocation)
			if (parent != null) parent.addToDataLocations(dataLocation)
		}
		dataLocation.addToChanges(change)
		
		if (checkTypeAndParent(type, parent, change, site)) {
			if (!type.equals(dataLocation.type)) {
				change.needsReview = true
				change.addToMessages "Type has changed to: ${type.code}"
			}
			if (!parent.equals(dataLocation.location)) {
				change.needsReview = true
				change.addToMessages "Parent has changed to: ${parent.code}"
			}
		}

		grailsApplication.config.i18nFields.locales.each{ loc ->
			dataLocation.setNames(site.name, new Locale(loc))
		}

		dataLocation.needsReview = change.needsReview
		dataLocation.itemid = site.id
		dataLocation.save(validate: false)
	}

	def deleteSite(def itemid) {
		log.debug("deleting site with itemid: ${itemid}")
		
		DataLocation dataLocation = DataLocation.findByItemid(itemid.toString())

		if (dataLocation == null) {
			log.info("deleting site that does not exist")
		}
		else {
			deleteDataLocation(dataLocation)
		}
	}
	
	def deleteDataLocation(def dataLocation) {
		SyncChange change = new SyncChange(needsReview: true)
		change.addToMessages "Data location was deleted."
		dataLocation.addToChanges(change)
		
		dataLocation.needsReview = true
		dataLocation.save(validate: false)
	}
	
}