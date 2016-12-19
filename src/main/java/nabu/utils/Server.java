package nabu.utils;

import java.lang.String;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.libs.types.api.KeyValuePair;
import be.nabu.libs.types.utils.KeyValuePairImpl;

@WebService
public class Server {
	
	public static char [] simplePasswordCharacters = new char [] {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
	};
	
	public static char [] complexPasswordCharacters = new char [] {
		'$', '*', '?', '!', '{', '}', '§', '^', '@', '#', '&', '[', ']', '(', ')' 
	};
	
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

	@WebResult(name = "uuid")
	public UUID uuid() {
		return UUID.randomUUID();
	}
	
	@WebResult(name = "password")
	public String password(@WebParam(name = "length") Integer length) {
		if (length == null) {
			length = 8;
		}
		StringBuilder builder = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			int nextInt = random.nextInt(simplePasswordCharacters.length);
			builder.append(simplePasswordCharacters[nextInt]);
		}
		return builder.toString();
	}
	
	@WebResult(name = "host")
	public String getHostName() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostName();
	}
	
	@WebResult(name = "server")
	public String getServerName() {
		return EAIResourceRepository.getInstance().getName();
	}
	
	@WebResult(name = "group")
	public String getServerGroup() {
		return EAIResourceRepository.getInstance().getGroup();
	}
	
	public void sleep(@WebParam(name = "amount") long amount, @WebParam(name = "unit") TimeUnit timeUnit) {
		try {
			Thread.sleep(timeUnit == null ? amount : TimeUnit.MILLISECONDS.convert(amount, timeUnit));
		}
		catch (InterruptedException e) {
			// continue
		}
	}
	
	@WebResult(name = "properties")
	public java.util.List<KeyValuePair> properties() {
		java.util.List<KeyValuePair> properties = new ArrayList<KeyValuePair>();
		Properties systemProperties = System.getProperties();
		for (java.lang.Object key : systemProperties.keySet()) {
			KeyValuePairImpl impl = new KeyValuePairImpl(key.toString(), systemProperties.getProperty(key.toString()));
			properties.add(impl);
		}
		return properties;
	}
	
	@WebResult(name = "value")
	public java.lang.String property(@NotNull @WebParam(name = "key") java.lang.String key, @WebParam(name = "defaultValue") java.lang.String defaultValue) {
		return System.getProperty(key, defaultValue);
	}
}
