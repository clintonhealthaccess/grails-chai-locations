package org.chai.location;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.FlushMode;

/**
 * Locations are nodes in the location tree. They can have a parent, and have a 
 * corresponding location level. If the location has no parent, it is considered
 * the root of the location tree. There can be only one root. The location tree
 * must also cannot contain cycles. Data locations can be attached to one and only one location.
 */
class Location extends CalculationLocation {

	Location parent
	LocationLevel level
	
	static hasMany = [dataLocations: DataLocation, children: Location]
	
	static constraints = {
		level nullable: false
		parent(nullable: true, validator: { val, obj ->
			// TODO validate that there are no loops, i.e the graph must be a DAG
			if (val == null) {
				Location.withSession { session ->
					def flushMode = session.getFlushMode()
					session.setFlushMode(FlushMode.MANUAL);
					def roots = Location.findAllByParentIsNull()
					session.setFlushMode(flushMode);
					return roots.empty || roots.equals([obj])
				}
			}
		})
	}
	
	static mapping = {
		table "chai_location_location"
		level column: "level"
		parent column: "parent"
		children cache: true
		dataLocations cache: true
	}
	
	@Override
	List<CalculationLocation> getChildren(def skipLevels) {
		def result = new ArrayList<Location>();
		for (def child : children) {
			if (skipLevels != null && skipLevels.contains(child.level)) {
				result.addAll(child.getChildren(skipLevels));
			}
			else result.add(child);
		}
		return result;
	}
	
	@Override
	List<DataLocation> getDataLocations(def skipLevels, def types) {
		List<DataLocation> result = new ArrayList<DataLocation>();
		for (DataLocation dataLocation : dataLocations) {
			if (types == null || types.contains(dataLocation.type)) 
				result.add(dataLocation);
		}
		
		for (Location child : children) {
			if (skipLevels != null && skipLevels.contains(child.level)) {
				result.addAll(child.getDataLocations(skipLevels, types));
			}
		}
		
		return result;				
	}
	
	@Override
	Location getParentOfLevel(LocationLevel level) {
		if (this.level.equals(level)) return this
		else return parent?.getParentOfLevel(level)
	}
	
	@Override
	boolean collectsData() {
		return false;
	}
	
	/**
	 * Returns all the direct children whose tree contain at least one data location. If this
	 * location is the parent of locations whose level is in the skipLevels list, it returns also the
	 * children's direct children who meets the same criteria.
	 *
	 * @param skipLevels if this location has children whose level is in this list, the children
	 * of those children are also returned. If skipLevels is {@code null}, skip levels are ignored.
	 * @param types returns only the data locations whose type is in this list, returns all data locations
	 * if types is {@code null}.
	 * @return all direct children whose tree contain at least one data location, taking into account the skip levels 
	 * and whose type is in the specified list
	 */
	List<CalculationLocation> getChildrenEntitiesWithDataLocations(def skipLevels, def types) {
		def result = new ArrayList<CalculationLocation>();
		
		def locationChildren = getChildren(skipLevels);
		def locationTree = collectTreeWithDataLocations(skipLevels, types);
		for(def locationChild : locationChildren){
			if(locationTree.contains(locationChild))
				result.add(locationChild);	
		}
		
		return result;
	}
	
	/**
	 * Collects all locations of the specified types that are children of this location and
	 * whose tree contains at least one data location whose type is in types. If types is
	 * {@code null}, returns all locations that are children of this location and whose tree contains
	 * at least one data location. Does not include the locations that are in the skipLevels list.
	 * 
	 * If data is true, also includes the data locations.
	 * 
	 * @param skipLevels ignores location whose level is in this list
	 * @param types only considers data location types that are in this list
	 * @return all locations of the specified types that are children of this location and whose tree contains at least
	 * one data location whose type is in types
	 */
	List<CalculationLocation> collectTreeWithDataLocations(def skipLevels, def types) {
		def locations = new ArrayList<Location>();
		collectLocations(locations, null, skipLevels, types);
		return locations;
	}
	
	String toString() {
		return "Location[Id=" + id + ", Code=" + code + "]";
	}

}