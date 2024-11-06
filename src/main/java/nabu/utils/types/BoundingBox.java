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

package nabu.utils.types;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "boundingBox")
public class BoundingBox {
	private Coordinate bottomLeft, topRight;

	public BoundingBox() {
		// automatic creation
	}
	
	public BoundingBox(Coordinate bottomLeft, Coordinate topRight) {
		this.bottomLeft = bottomLeft;
		this.topRight = topRight;
	}

	public Coordinate getBottomLeft() {
		return bottomLeft;
	}

	public void setBottomLeft(Coordinate bottomLeft) {
		this.bottomLeft = bottomLeft;
	}

	public Coordinate getTopRight() {
		return topRight;
	}

	public void setTopRight(Coordinate topRight) {
		this.topRight = topRight;
	}

	@Override
	public String toString() {
		return bottomLeft + "-" + topRight;
	}
	
}
