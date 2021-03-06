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

/**
 * Class for a data location. A data location is a location that collects data. It
 * has a parent location, a type and can be optionally managed by another data location.
 */
class DataLocation extends CalculationLocation {

	Long id

	Boolean needsReview
		
	Date dateCreated
	
	DataLocationType type
	Location location
	
	DataLocation managedBy
	
	static hasMany = [
		manages: DataLocation,
		changes: SyncChange
	]

	static mapping = {
		table "chai_location_data_location"
		type column: 'type'
		location column: 'location'
	}

	static constraints = {
		type nullable: false
		location nullable: false
		managedBy nullable: true
		needsReview nullable: true
	}
	
	@Override
	List<CalculationLocation> getChildren(def skipLevels) {
		return [];
	}
	
	@Override
	List<DataLocation> getDataLocations(def skipLevels, def types) {
		def result = new ArrayList<DataLocation>();
		if (types == null || types.contains(type)) result.add(this);
		return result;
	}
	
	@Override
	Location getParentOfLevel(LocationLevel level) {
		return this.location?.getParentOfLevel(level)
	}
	
	@Override
	boolean collectsData() {
		return true;
	}
	
	String toString() {
		return "DataLocation[Id=" + id + ", Code=" + code + "]";
	}
	
}
