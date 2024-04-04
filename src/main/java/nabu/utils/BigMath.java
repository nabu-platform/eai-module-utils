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
