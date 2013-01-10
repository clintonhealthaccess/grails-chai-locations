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

import groovy.transform.EqualsAndHashCode
import i18nfields.I18nFields


/**
 * Super class for all locations. Holds the name, the code (unique property) and the coordinates of
 * that location. 
 */
@EqualsAndHashCode(includes='code')
@i18nfields.I18nFields
abstract class CalculationLocation {

	String code
	String names
	String coordinates
	
	// we keep the facility registry itemid here
	// so that activity feed syncing works. this
	// information should not be visible to the user
	Long itemid
	
	static i18nFields = ['names']

	static constraints = {
		code nullable: false, blank: false, unique: true
		coordinates nullable: true, blank: true
		names nullable: true, blank: true
		itemid nullable: true
	}
	
	static mapping = {
		table "chai_location_abstract"
		tablePerSubclass true
		cache true
		coordinates type: "text"
		code unique: true, index: "Code_Index"
	}

	boolean collectLocations(List<Location> locations, List<DataLocation> dataLocations, def skipLevels, def types) {
		boolean result = false;
		for (Location child : getChildren(skipLevels)) {
			result = result | child.collectLocations(locations, dataLocations, skipLevels, types);
		}
		
		List<DataLocation> dataLocationsChildren = getDataLocations(skipLevels, types)
		if (!dataLocationsChildren.isEmpty()) {
			result = true;
			if (dataLocations != null) dataLocations.addAll(dataLocationsChildren);
		}
		
		if (result) {
			if (locations != null && !this.collectsData()) locations.add((Location) this);
		}
		return result;
	}
	
	/**
	 * Collects all data locations of the specified types that are children of this location. If types is
	 * {@code null}, returns all data locations that are children of this location.
	 *
	 * @return all data locations of the specified types.
	 */
	List<DataLocation> collectDataLocations(def types) {
		List<DataLocation> dataLocations = new ArrayList<DataLocation>();
		collectLocations(null, dataLocations, null, types);
		return dataLocations;
	}
	
	/**
	 * Returns all the data locations that are direct children of this location, and whose
	 * type is in the list specified by types. If this location is the parent of locations
	 * whose level is in the skipLevels list, it returns also the children's data locations.
	 *
	 * @param skipLevels if this location has children whose level is in this list, the data locations
	 * of those children are also returned. If skipLevels is {@code null}, skip levels are ignored.
	 * @param types returns only the data locations whose type is in this list, returns all data locations
	 * if types is {@code null}.
	 * @return the list of data locations.
	 */
	abstract List<DataLocation> getDataLocations(def skipLevels, def types)
	
	/**
	 * Returns all the location (data or not) that are direct children of this location. If this
	 * location is the parent of locations whose level is in the skipLevels list, it returns also
	 * the children's locations.
	 *
	 * @param skipLevels if this location has children whose level is in this list, the locations
	 * of those children are also returned. If skipLevels is {@code null}, skip levels are ignored.
	 * @return the list of locations
	 */
	abstract List<CalculationLocation> getChildren(def skipLevels)
	
	/**
	 * Navigates up through all the parents in the tree and returns the one with the specified level.
	 * If this location is of the specified level, it returns this location. If no parents have the
	 * specified level, it returns {@code null}.
	 *
	 * @param level the level of the parent we want to retrieve
	 * @return the parent with the specified level
	 */
	abstract Location getParentOfLevel(LocationLevel level)
	
	/**
	 * Returns true if this location collects data, false otherwise. If it returns
	 * true, then this object should be castable to DataLocation, otherwise
	 * it should cast to Location.
	 *
	 * @return true if this location collects data, false otherwise.
	 */
	abstract boolean collectsData();
	
	String getLabel() {
		return names.toString() + ' ['+code+'] ['+this.getClass().simpleName+']'
	}
	
}
