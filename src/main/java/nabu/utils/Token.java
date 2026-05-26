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

import java.security.Principal;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.eai.repository.util.SystemPrincipal;
import be.nabu.libs.authentication.api.Device;
import be.nabu.libs.authentication.api.WrappedToken;
import be.nabu.libs.authentication.api.principals.DevicePrincipal;
import be.nabu.libs.authentication.impl.BasicPrincipalImpl;
import be.nabu.libs.authentication.impl.ImpersonateToken;
import be.nabu.libs.services.api.ServiceDescription;

@WebService
public class Token {
	
	public static class DevicePrincipalImplementation implements DevicePrincipal {
		private java.lang.String name;
		private Device device;
		private static final long serialVersionUID = 1L;

		public DevicePrincipalImplementation() {
			// auto construct
		}
		
		public DevicePrincipalImplementation(java.lang.String name, Device device) {
			this.name = name;
			this.device = device;
		}

		@Override
		public java.lang.String getName() {
			return name;
		}

		@Override
		public Device getDevice() {
			return device;
		}

		public void setName(java.lang.String name) {
			this.name = name;
		}

		public void setDevice(Device device) {
			this.device = device;
		}
	}

	@ServiceDescription(comment = "Check whether {token|a token} is a system token")
	public boolean isSystemToken(@WebParam(name = "token") be.nabu.libs.authentication.api.Token token) {
		return token instanceof SystemPrincipal;
	}
	
	@ServiceDescription(comment = "Create a system token for {name|root} in {realm|a realm}")
	@WebResult(name = "token")
	public be.nabu.libs.authentication.api.Token newSystemToken(@WebParam(name = "realm") java.lang.String realm, @WebParam(name = "name") java.lang.String name) {
		if (realm == null && (name == null || name.equals("root"))) {
			return SystemPrincipal.ROOT;
		}
		else {
			return new SystemPrincipal(name, realm);
		}
	}
	
	@ServiceDescription(comment = "Unwrap {token|a token} if it is wrapped")
	@WebResult(name = "token")
	public be.nabu.libs.authentication.api.Token unwrap(@WebParam(name = "token") be.nabu.libs.authentication.api.Token token) {
		return token instanceof WrappedToken ? ((WrappedToken) token).getOriginalToken() : null;
	}
	
	@ServiceDescription(comment = "Wrap {token|a token} as {name|a name} in {realm|a realm}")
	@WebResult(name = "token")
	public be.nabu.libs.authentication.api.Token wrap(@WebParam(name = "token") be.nabu.libs.authentication.api.Token originalToken, @WebParam(name = "name") java.lang.String name, @WebParam(name = "realm") java.lang.String realm) {
		return new ImpersonateToken(originalToken, realm, name);
	}
	
	@ServiceDescription(comment = "Create a basic token for {name|a name}")
	@WebResult(name = "token")
	public be.nabu.libs.authentication.api.Token newBasicToken(@WebParam(name = "name") java.lang.String user, @WebParam(name = "password") java.lang.String password) {
		return new BasicPrincipalImpl(user, password);
	}
	
	@ServiceDescription(comment = "Create a device principal for {name|a name} on {device|a device}")
	@WebResult(name = "principal")
	public Principal newDevicePrincipal(final @NotNull @WebParam(name = "name") java.lang.String name, final @NotNull @WebParam(name = "device") Device device) {
		return new DevicePrincipalImplementation(name, device);
	}
	
}
