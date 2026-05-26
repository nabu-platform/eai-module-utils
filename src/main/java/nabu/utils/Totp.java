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

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.libs.services.api.ServiceDescription;
import be.nabu.utils.security.TOTP;

@WebService
public class Totp {
	
	@ServiceDescription(comment = "Generate a new TOTP key")
	@WebResult(name = "key")
	public java.lang.String newKey() {
		return TOTP.generateKey();
	}
	
	@ServiceDescription(comment = "Generate an OTP for {key|a key}")
	@WebResult(name = "otp")
	public java.lang.String otp(@WebParam(name = "key") java.lang.String key) {
		return TOTP.getOtp(key);
	}
	
}
