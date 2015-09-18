package nabu.utils;

import java.io.IOException;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import java.lang.Object;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import nabu.types.Property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Node;
import be.nabu.eai.repository.artifacts.broker.DefinedBrokerClient;
import be.nabu.libs.artifacts.ArtifactResolverFactory;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;

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
	
	public List<ComplexType> getComplexTypes() {
		List<ComplexType> artifacts = new ArrayList<ComplexType>();
		EAIResourceRepository instance = EAIResourceRepository.getInstance();
		if (instance != null) {
			for (Node node : instance.getNodes(DefinedType.class)) {
				try {
					if (node.getArtifact() instanceof ComplexType) {
						artifacts.add((ComplexType) node.getArtifact());
					}
				}
				catch (Exception e) {
					logger.error("Could not load: " + node, e);
				}
			}
		}
		return artifacts;
	}
}
