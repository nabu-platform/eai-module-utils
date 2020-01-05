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

	public boolean isSystemToken(@WebParam(name = "token") be.nabu.libs.authentication.api.Token token) {
		return token instanceof SystemPrincipal;
	}
	
	@WebResult(name = "token")
	public be.nabu.libs.authentication.api.Token newSystemToken(@WebParam(name = "realm") java.lang.String realm, @WebParam(name = "name") java.lang.String name) {
		if (realm == null && (name == null || name.equals("root"))) {
			return SystemPrincipal.ROOT;
		}
		else {
			return new SystemPrincipal(name, realm);
		}
	}
	
	@WebResult(name = "token")
	public be.nabu.libs.authentication.api.Token unwrap(@WebParam(name = "token") be.nabu.libs.authentication.api.Token token) {
		return token instanceof WrappedToken ? ((WrappedToken) token).getOriginalToken() : null;
	}
	
	@WebResult(name = "token")
	public be.nabu.libs.authentication.api.Token wrap(@WebParam(name = "token") be.nabu.libs.authentication.api.Token originalToken, @WebParam(name = "name") java.lang.String name, @WebParam(name = "realm") java.lang.String realm) {
		return new ImpersonateToken(originalToken, realm, name);
	}
	
	@WebResult(name = "token")
	public be.nabu.libs.authentication.api.Token newBasicToken(@WebParam(name = "name") java.lang.String user, @WebParam(name = "password") java.lang.String password) {
		return new BasicPrincipalImpl(user, password);
	}
	
	@WebResult(name = "principal")
	public Principal newDevicePrincipal(final @NotNull @WebParam(name = "name") java.lang.String name, final @NotNull @WebParam(name = "device") Device device) {
		return new DevicePrincipalImplementation(name, device);
	}
}
