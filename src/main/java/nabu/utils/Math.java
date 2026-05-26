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

import java.math.BigDecimal;
import be.nabu.libs.services.api.ServiceDescription;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Random;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService
public class Math {
	
	@ServiceDescription(comment = "Round {value|a number} to {precision|zero} decimals")
	@WebResult(name = "rounded")
	public Double round(@WebParam(name = "value") Double value, @WebParam(name = "precision") Integer precision, @WebParam(name = "roundingMode") RoundingMode roundingMode) {
		if (value == null) {
			return null;
		}
		BigDecimal bigDecimal = BigDecimal.valueOf(value);
		return bigDecimal.setScale(precision == null ? 0 : precision, roundingMode == null ? RoundingMode.HALF_UP : roundingMode).doubleValue();
	}
	
	@ServiceDescription(comment = "Get the absolute values of {values|numbers}")
	@WebResult(name = "absolute")
	public java.util.List<Double> absolute(@WebParam(name = "values") java.util.List<Double> values) {
		java.util.List<Double> result = new ArrayList<Double>();
		for (Double value : values) {
			result.add(value == null ? value : (Double) java.lang.Math.abs(value));
		}
		return result;
	}
	
	@ServiceDescription(comment = "Sum {values|numbers}")
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
	
	@ServiceDescription(comment = "Multiply {values|numbers}")
	@WebResult(name = "result")
	public Double multiply(@WebParam(name = "values") java.util.List<Double> values) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		double result = 1;
		for (Double value : values) {
			if (value != null) {
				result *= value;
			}
		}
		return result;
	}
	
	@ServiceDescription(comment = "Get the maximum of {values|numbers}")
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
	
	@ServiceDescription(comment = "Get the minimum of {values|numbers}")
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
	
	@ServiceDescription(comment = "Calculate the average of {values|numbers}")
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
	@ServiceDescription(comment = "Calculate the variance of {values|numbers}")
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
	@ServiceDescription(comment = "Calculate the standard deviation of {values|numbers}")
	@WebResult(name = "deviation")
	public Double deviation(@WebParam(name = "values") java.util.List<Double> values, @WebParam(name = "sample") Boolean isSample) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		return java.lang.Math.sqrt(variance(values, isSample));
	}

	@ServiceDescription(comment = "Generate a random number between {minimum|zero} and {maximum|one}")
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
