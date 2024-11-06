/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package nabu.utils;

import junit.framework.TestCase;
import nabu.utils.types.Coordinate;

import java.lang.Math;

public class GisTest extends TestCase {
	
	public void testDistance() {
		Coordinate eiffelTower = new Coordinate(48.8583, 2.2945);
		Coordinate statueOfLiberty = new Coordinate(40.6892, -74.0444);
		long rounded = Math.round(new Gis().distance(eiffelTower, statueOfLiberty));
		assertEquals(5837, rounded);
	}
	
	public void testBoundingBox() {
		Coordinate eiffelTower = new Coordinate(48.8583, 2.2945);
		System.out.println(new Gis().boundingBox(eiffelTower, 50));
		
		Coordinate mannekePis = new Coordinate(50.8449967, 4.3477891);
		System.out.println(new Gis().boundingBox(mannekePis, 50));
		
		Coordinate sydneyOpera = new Coordinate(-33.8568935, 151.2130919);
		System.out.println(new Gis().boundingBox(sydneyOpera, 50));
	}
}
