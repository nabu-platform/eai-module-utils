package nabu.utils;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.utils.security.TOTP;

@WebService
public class Totp {
	
	@WebResult(name = "key")
	public java.lang.String newKey() {
		return TOTP.generateKey();
	}
	
	@WebResult(name = "otp")
	public java.lang.String otp(@WebParam(name = "key") java.lang.String key) {
		return TOTP.getOtp(key);
	}
	
}
