package nabu.utils;

import java.io.IOException;
import java.lang.String;
import java.util.List;
import java.lang.Object;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.repository.artifacts.broker.DefinedBrokerClient;
import be.nabu.libs.artifacts.ArtifactResolverFactory;

@WebService
public class Server {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public void log(@WebParam(name = "message") String message) {
		if (message != null) {
			logger.info(message);
		}
	}
	
	public void publish(@NotNull @WebParam(name = "brokerClientId") String brokerClientId, @WebParam(name = "content") Object content, @WebParam(name = "properties") List<Property> properties) throws IOException {
		DefinedBrokerClient brokerClient = (DefinedBrokerClient) ArtifactResolverFactory.getInstance().getResolver().resolve(brokerClientId);
		if (brokerClient == null) {
			throw new IllegalArgumentException("The broker client can not be found: " + brokerClientId);
		}
		if (content != null) {
			brokerClient.getBrokerClient().publish(content, new Properties().toMap(properties));
		}
	}
}
