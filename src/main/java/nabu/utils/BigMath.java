package nabu.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
}
