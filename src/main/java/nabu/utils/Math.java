package nabu.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService
public class Math {
	
	@WebResult(name = "rounded")
	public Double round(@WebParam(name = "value") Double value, @WebParam(name = "precision") Integer precision, @WebParam(name = "roundingMode") RoundingMode roundingMode) {
		if (value == null) {
			return null;
		}
		BigDecimal bigDecimal = BigDecimal.valueOf(value);
		return bigDecimal.setScale(precision == null ? 0 : precision, roundingMode == null ? RoundingMode.HALF_UP : roundingMode).doubleValue();
	}
	
	@WebResult(name = "sum")
	public Double sum(@WebParam(name = "values") java.util.List<Double> values) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		double sum = 0;
		for (Double value : values) {
			if (value != null) {
				sum += value;
			}
		}
		return sum;
	}
	
	@WebResult(name = "maximum")
	public Double maximum(@WebParam(name = "values") java.util.List<Double> values) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		Double max = null;
		for (Double value : values) {
			if (value != null) {
				if (max == null || value > max) {
					max = value;
				}
			}
		}
		return max;
	}
	
	@WebResult(name = "minimum")
	public Double minimum(@WebParam(name = "values") java.util.List<Double> values) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		Double min = null;
		for (Double value : values) {
			if (value != null) {
				if (min == null || value < min) {
					min = value;
				}
			}
		}
		return min;
	}
	
	@WebResult(name = "average")
	public Double average(@WebParam(name = "values") java.util.List<Double> values) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		double sum = 0.0;
		for (Double value : values) {
			sum += value;
		}
		return sum / values.size();
	}
	
	// we either have a population calculation (the values are the entirety of the available values) or a sample calculation where the value represent only a subset of the real series of values
	@WebResult(name = "variance")
	public Double variance(@WebParam(name = "values") java.util.List<Double> values, @WebParam(name = "sample") Boolean isSample) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		double average = average(values);
		double sum = 0.0;
		for (Double value : values) {
			sum += java.lang.Math.pow(value - average, 2);
		}
		return sum / (isSample != null && isSample ? values.size() - 1 : values.size());
	}
	
	// standard deviation
	@WebResult(name = "deviation")
	public Double deviation(@WebParam(name = "values") java.util.List<Double> values, @WebParam(name = "sample") Boolean isSample) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		return java.lang.Math.sqrt(variance(values, isSample));
	}

	@WebResult(name = "random")
	public Double random(@WebParam(name = "minimum") Double minimum, @WebParam(name = "maximum") Double maximum) {
		double nextDouble = new Random().nextDouble();
		if (maximum != null) {
			nextDouble *= minimum == null ? maximum : maximum - minimum;
		}
		if (minimum != null) {
			nextDouble += minimum;
		}
		return nextDouble;
	}
}
