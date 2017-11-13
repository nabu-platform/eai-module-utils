package nabu.utils;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.eai.repository.util.SystemPrincipal;
import be.nabu.libs.authentication.api.WrappedToken;
import be.nabu.libs.authentication.impl.ImpersonateToken;

@WebService
public class Token {
	
	public boolean isSystemToken(@WebParam(name = "token") be.nabu.libs.authentication.api.Token token) {
		return token instanceof SystemPrincipal;
	}
	
	@WebResult(name = "token")
	public be.nabu.libs.authentication.api.Token newSystemToken(@WebParam(name = "realm") java.lang.String realm, @WebParam(name = "name") java.lang.String name) {
		if (realm == null && name.equals("root")) {
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
	
}
