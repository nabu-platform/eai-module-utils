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
