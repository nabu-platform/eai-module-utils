package nabu.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
	
	@WebResult(name = "variance")
	public Double variance(@WebParam(name = "values") java.util.List<Double> values) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		double average = average(values);
		double sum = 0.0;
		for (Double value : values) {
			sum += java.lang.Math.pow(value - average, 2);
		}
		return sum / values.size();
	}
	
	// standard deviation
	@WebResult(name = "deviation")
	public Double deviation(@WebParam(name = "values") java.util.List<Double> values) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		return java.lang.Math.sqrt(variance(values));
	}
	
}
