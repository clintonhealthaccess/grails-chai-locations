package org.chai.location

import grails.plugin.spock.UnitSpec;

class CalculationLocationUnitSpec extends UnitSpec {

	def "test equals"() {
		when:
		def level = new LocationLevel(code: 'level1')
		
		then:
		!level.equals(new LocationLevel(code: 'level2'))
		
		when:
		def location = new Location(code: 'location1')
		
		then:
		!location.equals(new Location(code: 'location2'))
	}
	
	
	def "get data entities on location location"() {
		when:
		def rwanda = new Location(code: "rwanda")
		def north = new Location(code: "north", parent: rwanda)
		def burera = new Location(code: "burera", parent: north)
		rwanda.children = [north]
		north.children = [burera]
		
		def data1 = new DataLocation(code: 'data1', location: north);
		north.dataLocations = [data1]
		def data2 = new DataLocation(code: 'data2', location: burera);
		burera.dataLocations = [data2]
		
		then:
		rwanda.getDataLocations(null, null).empty
		north.getDataLocations(null, null).equals([data1])
		burera.getDataLocations(null, null).equals([data2])
	}

	def "get data entities on location entity with skip"() {
		setup:
		def country = new LocationLevel(code: "country")
		def province = new LocationLevel(code: "province")
		def district = new LocationLevel(code: "district")
		
		when:
		def rwanda = new Location(code: "rwanda", level: country)
		def north = new Location(code: "north", parent: rwanda, level: province)
		def burera = new Location(code: "burera", parent: north, level: district)
		rwanda.children = [north]
		north.children = [burera]
		
		def data1 = new DataLocation(code: 'data1', location: north);
		north.dataLocations = [data1]
		def data2 = new DataLocation(code: 'data2', location: burera);
		burera.dataLocations = [data2]
		
		then:
		rwanda.getDataLocations(new HashSet([province]), null).equals([data1])
		rwanda.getDataLocations(new HashSet([province,district]), null).equals([data1, data2])
		north.getDataLocations(new HashSet([district]), null).equals([data1, data2])
	}
		
	def "collect data entities on location entity with specific type"() {
		when:
		def rwanda = new Location(code: "rwanda")
		def north = new Location(code: "north", parent: rwanda)
		def burera = new Location(code: "burera", parent: north)		
		rwanda.children = [north]
		north.children = [burera]

		then:
		rwanda.collectTreeWithDataLocations(null, null).empty
		rwanda.collectDataLocations(null).empty
		
		when:
		def type1 = new DataLocationType(code: 'type1');
		def data1 = new DataLocation(code: 'data1', location: north, type: type1);
		north.dataLocations = [data1]
		
		then:
		rwanda.collectTreeWithDataLocations(null, null).equals([north, rwanda])
		rwanda.collectDataLocations(null).equals([data1])
		rwanda.collectTreeWithDataLocations(null, new HashSet([type1])).equals([north, rwanda])
		rwanda.collectDataLocations(new HashSet([type1])).equals([data1])
		
		when:
		def type2 = new DataLocationType(code: 'type2')

		then:
		rwanda.collectTreeWithDataLocations(null, new HashSet([type2])).empty
		rwanda.collectDataLocations(new HashSet([type2])).empty
		
	}
	
	def "collect data entities on location at different levels"() {
		when:
		def rwanda = new Location(code: "rwanda")
		def north = new Location(code: "north", parent: rwanda)
		def burera = new Location(code: "burera", parent: north)
		rwanda.children = [north]
		north.children = [burera]
		
		def data1 = new DataLocation(code: 'data1', location: north);
		north.dataLocations = [data1]
		def data2 = new DataLocation(code: 'data2', location: burera);
		burera.dataLocations = [data2]
		
		then:
		rwanda.collectDataLocations(null).equals([data2, data1])
	}
	
	def "collect data entities for on data location entity"() {
		when:
		def type1 = new DataLocationType(code: 'type1');
		def data1 = new DataLocation(code: 'data1', type: type1);
		
		then:
		data1.collectDataLocations(new HashSet([type1])).equals([data1])
		
		when:
		def type2 = new DataLocationType(code: 'type2');
		
		then:
		data1.collectDataLocations(new HashSet([type2])).empty
	}
	
	def "get data entities on data entitiy"() {
		when:
		def data1 = new DataLocation(code: 'data1');
		
		then:
		data1.getDataLocations(null, null).equals([data1])
	}
	
	def "get data entities of type on data entity"() {
		when:
		def type1 = new DataLocationType(code: 'type1');
		def data1 = new DataLocation(code: 'data1', type: type1);
		
		then:
		data1.getDataLocations(null, new HashSet([type1])).equals([data1])
		
		when:
		def type2 = new DataLocationType(code: 'type2');
		
		then:
		data1.getDataLocations(null, new HashSet([type2])).empty
	}
	
	def "test get children with skip on location entity"() {
		setup:
		def country = new LocationLevel(code: "country")
		def province = new LocationLevel(code: "province")
		def district = new LocationLevel(code: "district")
		
		when:
		def rwanda = new Location(code: "rwanda", level: country)
		def north = new Location(code: "north", parent: rwanda, level: province)
		def burera = new Location(code: "burera", parent: north, level: district)
		rwanda.children = [north]
		north.children = [burera]
		
		then:
		rwanda.getChildren(new HashSet([province])).equals([burera])
		rwanda.getChildren(new HashSet([province, district])).empty
	}
	
	def "test get data entities with skip on location entity"() {
		setup:
		def country = new LocationLevel(code: "country")
		def province = new LocationLevel(code: "province")
		def district = new LocationLevel(code: "district")
		
		when:
		def rwanda = new Location(code: "rwanda", level: country)
		def north = new Location(code: "north", parent: rwanda, level: province)
		def burera = new Location(code: "burera", parent: north, level: district)
		rwanda.children = [north]
		north.children = [burera]
		
		def dataDistrict = new DataLocation(code: 'data1', location: burera);
		burera.dataLocations = [dataDistrict]
		
		then:
		rwanda.getDataLocations(new HashSet([province]), null).empty
		rwanda.getDataLocations(new HashSet([province, district]), null).equals([dataDistrict])
		north.getDataLocations(new HashSet([province]), null).empty
		
		when:
		def dataProvince = new DataLocation(code: 'data2', location: north);
		north.dataLocations = [dataProvince]
		
		then:
		rwanda.getDataLocations(new HashSet([province]), null).equals([dataProvince])
		rwanda.getDataLocations(new HashSet([province, district]), null).equals([dataProvince, dataDistrict])
		north.getDataLocations(new HashSet([province]), null).equals([dataProvince])
		
		when:
		def dataCountry = new DataLocation(code: 'data3', location: rwanda)
		rwanda.dataLocations = [dataCountry]
		
		then:
		rwanda.getDataLocations(new HashSet([province]), null).equals([dataCountry, dataProvince])
		rwanda.getDataLocations(new HashSet([province, district]), null).equals([dataCountry, dataProvince, dataDistrict])
		north.getDataLocations(new HashSet([province]), null).equals([dataProvince])
	}
	
	def "test get children entity with skip"() {
		setup:
		def country = new LocationLevel(code: "country")
		def province = new LocationLevel(code: "province")
		def district = new LocationLevel(code: "district")
		def type1 = new DataLocationType(code: 'type1')
		def type2 = new DataLocationType(code: 'type2')
		
		when:
		def rwanda = new Location(code: "rwanda", level: country)
		def north = new Location(code: "north", parent: rwanda, level: province)
		def burera = new Location(code: "burera", parent: north, level: district)
		rwanda.children = [north]
		north.children = [burera]
		
		def data1 = new DataLocation(code: 'data1', location: north, type: type1)
		north.dataLocations = [data1]
		
		def data2 = new DataLocation(code: 'data2', location: burera, type: type2)
		burera.dataLocations = [data2]
		
		then:
		rwanda.getDataLocations(new HashSet([province]), new HashSet([type1, type2])).equals([data1])
		rwanda.getChildren(new HashSet([province])).equals([burera])
		rwanda.getDataLocations(new HashSet([province, district]), new HashSet([type1, type2])).equals([data1, data2])
		rwanda.getChildren(new HashSet([province, district])).equals([])	
	}
	
	def "get parent of level"() {
		setup:
		def country = new LocationLevel(code: "country")
		def province = new LocationLevel(code: "province")
		def district = new LocationLevel(code: "district")

		when:
		def rwanda = new Location(code: "rwanda", level: country)
		def north = new Location(code: "north", parent: rwanda, level: province)
		def burera = new Location(code: "burera", parent: north, level: district)
	
		then:
		rwanda.getParentOfLevel(country) == rwanda
		rwanda.getParentOfLevel(province) == null
		burera.getParentOfLevel(country) == rwanda
		burera.getParentOfLevel(province) == north
		burera.getParentOfLevel(district) == burera
	}
	
	def "test get children entities with data locations"() {
		setup:
		def country = new LocationLevel(code: "country")
		def province = new LocationLevel(code: "province")
		def district = new LocationLevel(code: "district")
		def type1 = new DataLocationType(code: 'type1')
		def type2 = new DataLocationType(code: 'type2')
		def types = new HashSet([type1, type2])
		
		when:
		def rwanda = new Location(code: "rwanda", level: country)
		def south = new Location(code: "south", parent: rwanda, level: province)
		def north = new Location(code: "north", parent: rwanda, level: province)
		def burera = new Location(code: "burera", parent: north, level: district)
		
		def data1 = new DataLocation(code: 'data1', location: rwanda, type: type1)
		def data2 = new DataLocation(code: 'data2', location: burera, type: type2)
		
		rwanda.children = [north, south]
		rwanda.dataLocations = [data1]
		north.children = [burera]
		burera.dataLocations = [data2]
		
		then:
		rwanda.getChildrenEntitiesWithDataLocations(null, types).equals([north])
		rwanda.getChildrenEntitiesWithDataLocations(new HashSet([province]), types).equals([burera])
		rwanda.getChildrenEntitiesWithDataLocations(new HashSet([province, district]), types).equals([])
	}
	
	def "test collect locations on DataLocation"() {
		setup:
		def rwanda = new Location(code: "rwanda")
		def type1 = new DataLocationType(code: 'type1')
		def data1 = new DataLocation(code: 'data1', location: rwanda, type: type1);
		rwanda.dataLocations = [data1]
		def locations = [] 
		def dataLocations = []
		
		when:
		data1.collectLocations(locations, dataLocations, null, new HashSet([type1]))
		
		then:
		dataLocations == [data1]
	}
	
	def "test collect locations with skip levels"() {
		setup:
		def country = new LocationLevel(code: "country")
		def province = new LocationLevel(code: "province")
		def district = new LocationLevel(code: "district")
		def rwanda = new Location(code: "rwanda", level: country)
		def north = new Location(code: "north", parent: rwanda, level: province)
		def burera = new Location(code: "burera", parent: north, level: district)
		rwanda.children = [north]
		north.children = [burera]
		def type1 = new DataLocationType(code: 'type1')
		def data1 = new DataLocation(code: 'data1', location: burera, type: type1)
		burera.dataLocations = [data1]
		def locations 
		def dataLocations 
		
		when:
		locations = []
		dataLocations = []
		rwanda.collectLocations(locations, dataLocations, new HashSet([province]), null)
		
		then:
		locations == [burera, rwanda]
		dataLocations == [data1]
		
		when:
		def type2 = new DataLocationType(code: 'type2')
		def data2 = new DataLocation(code: 'data2', location: rwanda, type: type2)
		rwanda.dataLocations = [data2]
		locations = []
		dataLocations = []
		rwanda.collectLocations(locations, dataLocations, new HashSet([district]), new HashSet([type1]))
		
		then:
		locations == [north, rwanda]
		dataLocations == [data1]
	}
	
}
