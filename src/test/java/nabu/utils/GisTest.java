package nabu.utils;

import junit.framework.TestCase;
import nabu.types.Coordinate;

public class GisTest extends TestCase {
	
	public void testDistance() {
		Coordinate eiffelTower = new Coordinate(48.8583, 2.2945);
		Coordinate statueOfLiberty = new Coordinate(40.6892, -74.0444);
		long rounded = Math.round(new Gis().getDistance(eiffelTower, statueOfLiberty));
		assertEquals(5837, rounded);
	}
	
	public void testBoundingBox() {
		Coordinate eiffelTower = new Coordinate(48.8583, 2.2945);
		System.out.println(new Gis().getBoundingBox(eiffelTower, 50));
		
		Coordinate mannekePis = new Coordinate(50.8449967, 4.3477891);
		System.out.println(new Gis().getBoundingBox(mannekePis, 50));
		
		Coordinate sydneyOpera = new Coordinate(-33.8568935, 151.2130919);
		System.out.println(new Gis().getBoundingBox(sydneyOpera, 50));
	}
}
