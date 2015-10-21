package nabu.utils;

import java.io.IOException;
import java.lang.String;
import java.util.List;
import java.util.UUID;
import java.lang.Object;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import nabu.types.Property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.repository.artifacts.broker.DefinedBrokerClient;
import be.nabu.libs.artifacts.ArtifactResolverFactory;

@WebService
public class Server {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public void log(@WebParam(name = "message") String message, @WebParam(name = "logger") String loggerName, @WebParam(name = "exception") Exception exception) {
		if (message != null) {
			if (loggerName != null) {
				if (exception != null) {
					LoggerFactory.getLogger(loggerName).error(message, exception);
				}
				else {
					LoggerFactory.getLogger(loggerName).info(message);
				}
			}
			else {
				if (exception != null) {
					logger.error(message, exception);
				}
				else {
					logger.info(message);
				}
			}
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
	
	@WebResult(name = "uuid")
	public UUID uuid() {
		return UUID.randomUUID();
	}
	
	@WebResult(name = "host")
	public String getHostName() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostName();
	}
}
