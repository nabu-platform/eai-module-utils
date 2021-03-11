package nabu.utils;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import java.lang.Math;

import javax.validation.constraints.NotNull;

import nabu.utils.types.BoundingBox;
import nabu.utils.types.Coordinate;
import static java.lang.Math.*;

@WebService
public class Gis {
	
	/**
	 * Expressed in kilometers
	 */
	public static final double EARTH_RADIUS = 6371;
	
	/**
	 * -pi / 2
	 */
	public static final double MIN_LATITUDE = Math.toRadians(-90d);
	/**
	 * pi / 2
	 */
	public static final double MAX_LATITUDE = Math.toRadians(90d);
	/**
	 * -pi
	 */
	public static final double MIN_LONGITUDE = Math.toRadians(-180d);
	/**
	 * pi
	 */
	public static final double MAX_LONGITUDE = Math.toRadians(180d); 
	
	@WebResult(name = "boundingBox")
	public BoundingBox boundingBox(@NotNull @WebParam(name = "center") Coordinate center, @WebParam(name = "distance") long distance) {
		// based on: http://janmatuschek.de/LatitudeLongitudeBoundingCoordinates
		// according to Haversine we can have a central angle by doing d / r (distance and radius)
		double angularRadius = distance / EARTH_RADIUS;

		double minLatitude = toRadians(center.getLatitude()) - angularRadius;
		double maxLatitude = toRadians(center.getLatitude()) + angularRadius;
		
		double minLongitude, maxLongitude;
		if (minLatitude > MIN_LATITUDE && maxLatitude < MAX_LATITUDE) {
			double deltaLongitude = asin(sin(angularRadius) / cos(toRadians(center.getLatitude())));
			minLongitude = toRadians(center.getLongitude()) - deltaLongitude;
			maxLongitude = toRadians(center.getLongitude()) + deltaLongitude;
			if (minLongitude < MIN_LONGITUDE) {
				minLongitude += 2d * PI;
			}
			if (maxLongitude > MAX_LONGITUDE) {
				minLongitude -= 2d * PI;
			}
		}
		// there is a pole in the angular radius
		else {
			minLatitude = max(minLatitude, MIN_LATITUDE);
			maxLatitude = min(maxLatitude, MAX_LATITUDE);
			minLongitude = MIN_LONGITUDE;
			maxLongitude = MAX_LONGITUDE;
		}
		return new BoundingBox(
			new Coordinate(toDegrees(minLatitude), toDegrees(minLongitude)),
			new Coordinate(toDegrees(maxLatitude), toDegrees(maxLongitude))
		);
	}
	
	@WebResult(name = "distance")	// in meters it seems!
	public double distance(@NotNull @WebParam(name = "from") Coordinate from, @NotNull @WebParam(name = "to") Coordinate to) {
		// based on https://en.wikipedia.org/wiki/Haversine_formula
		// and http://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java
		double latitudeFrom = toRadians(from.getLatitude());
		double latitudeTo = toRadians(to.getLatitude());
		double latitudeDiff = toRadians(to.getLatitude() - from.getLatitude());
		double longitudeDiff = toRadians(to.getLongitude() - from.getLongitude());

		// first we calculate the central angle
		double centralAngle = haversine(latitudeDiff) + cos(latitudeFrom) * cos(latitudeTo) * haversine(longitudeDiff);
		
		// which is the same as haversine(d / r)
		// where d = the distance between the points
		// and r = the radius of the sphere
		
		// not entirely sure how this fits with the wiki page on haversine
		// i would expect something like "2 * earthRadius * sqrt(asin(centralAngle))"
		double distance =  2 * EARTH_RADIUS * atan2(sqrt(centralAngle), sqrt(1 - centralAngle));
		
		return distance;
	}
	
	private double haversine(double theta) {
		return sin(theta / 2) * sin(theta / 2); 
	}
	
	@WebResult(name = "coordinate")
	public Coordinate convertLambert72(@WebParam(name = "x") double x, @WebParam(name = "y") double y) {
		double n = 0.77164219;
		double F = 1.81329763;
		double thetaFudge = 0.00014204;
		double e = 0.08199189;
		double a = 6378388.0;
		double xDiff = 149910.0;
		double yDiff = 5400150.0;

		double theta0 = 0.07604294;

		double xReal = xDiff - x;
		double yReal = yDiff - y;

		double rho = sqrt((xReal * xReal) + (yReal * yReal));
		double theta = atan(xReal / (-yReal));

		double pi = 3.141592653589793238462643383279;

		double newLongitude = (theta0 + (theta + thetaFudge) / n) * 180 / pi;
		double newLatitude = 0.0;

		
		for (int i = 0; i < 5; i++) {
			newLatitude = 2.0 * atan(pow((F * a / rho), (1.0 / n)) * pow(((1.0 + e * sin(newLatitude)) / (1.0 - e * sin(newLatitude))), (e / 2.0))) - (pi / 2.0);
		}

		newLatitude = newLatitude * (180.0 / pi);

		return new Coordinate(newLatitude, newLongitude);
	}
}
