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
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService
public class BigMath {
	@WebResult(name = "rounded")
	public BigDecimal round(@WebParam(name = "value") BigDecimal bigDecimal, @WebParam(name = "precision") Integer precision, @WebParam(name = "roundingMode") RoundingMode roundingMode) {
		if (bigDecimal == null) {
			return null;
		}
		return bigDecimal.setScale(precision == null ? 0 : precision, roundingMode == null ? RoundingMode.HALF_UP : roundingMode);
	}
	
	@WebResult(name = "absolute")
	public java.util.List<BigDecimal> absoluteDecimals(@WebParam(name = "decimals") java.util.List<BigDecimal> bigDecimal) {
		if (bigDecimal == null) {
			return null;
		}
		ArrayList<BigDecimal> result = new ArrayList<BigDecimal>();
		for (BigDecimal decimal : bigDecimal) {
			if (decimal != null) {
				result.add(decimal.abs());
			}
		}
		return result;
	}
	
	@WebResult(name = "absolute")
	public java.util.List<BigInteger> absoluteIntegers(@WebParam(name = "decimals") java.util.List<BigInteger> bigInteger) {
		if (bigInteger == null) {
			return null;
		}
		ArrayList<BigInteger> result = new ArrayList<BigInteger>();
		for (BigInteger integer : bigInteger) {
			result.add(integer.abs());
		}
		return result;
	}
	
	@WebResult(name = "result")
	public BigDecimal sumDecimals(@WebParam(name = "decimals") java.util.List<BigDecimal> decimals) {
		BigDecimal result = BigDecimal.valueOf(0);
		if (decimals != null) {
			for (BigDecimal decimal : decimals) {
				if (decimal != null) {
					result = result.add(decimal);
				}
			}
		}
		return result;
	}
	
	@WebResult(name = "result")
	public BigInteger sumIntegers(@WebParam(name = "integers") java.util.List<BigInteger> integers) {
		BigInteger result = BigInteger.valueOf(0);
		if (integers != null) {
			for (BigInteger single : integers) {
				if (single != null) {
					result = result.add(single);
				}
			}
		}
		return result;
	}
	
	@WebResult(name = "result")
	public BigDecimal multiplyDecimals(@WebParam(name = "decimals") java.util.List<BigDecimal> decimals) {
		BigDecimal result = BigDecimal.valueOf(1);
		if (decimals != null) {
			for (BigDecimal decimal : decimals) {
				if (decimal != null) {
					result = result.multiply(decimal);
				}
			}
		}
		return result;
	}
	
	@WebResult(name = "result")
	public BigInteger multiplyIntegers(@WebParam(name = "integers") java.util.List<BigInteger> integers) {
		BigInteger result = BigInteger.valueOf(1);
		if (integers != null) {
			for (BigInteger single : integers) {
				if (single != null) {
					result = result.multiply(single);
				}
			}
		}
		return result;
	}
}
