package nabu.artifacts;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import nabu.types.WebArtifactInformation;
import be.nabu.eai.repository.artifacts.web.WebArtifact;
import be.nabu.libs.services.api.ExecutionContext;

@WebService
public class Web {
	
	private ExecutionContext executionContext;
	
	@WebResult(name = "information")
	public WebArtifactInformation information(@NotNull @WebParam(name = "artifactId") String id) throws IOException {
		if (id != null) {
			WebArtifact resolved = executionContext.getServiceContext().getResolver(WebArtifact.class).resolve(id);
			if (resolved != null) {
				WebArtifactInformation information = new WebArtifactInformation();
				information.setRealm(resolved.getRealm());
				information.setCharset(resolved.getConfiguration().getCharset() == null ? Charset.defaultCharset() : Charset.forName(resolved.getConfiguration().getCharset()));
				information.setHosts(resolved.getConfiguration().getHosts() == null ? null : new ArrayList<String>(resolved.getConfiguration().getHosts()));
				information.setPath(resolved.getConfiguration().getPath());
				information.setPort(resolved.getConfiguration().getHttpServer() == null ? null : resolved.getConfiguration().getHttpServer().getConfiguration().getPort());
				information.setSecure(resolved.getConfiguration().getHttpServer() == null ? null : resolved.getConfiguration().getHttpServer().getConfiguration().getKeystore() != null);
				return information;
			}
		}
		return null;
	}
	
	
}
