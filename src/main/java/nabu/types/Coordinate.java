package nabu.types;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "coordinate")
public class Coordinate {
	private double latitude, longitude;
	
	public Coordinate() {
		// empty constructor
	}
	
	public Coordinate(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	@Override
	public String toString() {
		return "[" + latitude + ", " + longitude + "]";
	}
	
}
