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
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.ServiceUtils;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceDescription;
import be.nabu.libs.services.api.ServiceRunner;
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
	
	public enum LogLevel {
		ERROR,
		WARN,
		INFO,
		DEBUG
	}
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public void log(@WebParam(name = "message") String message, @WebParam(name = "logger") String loggerName, @WebParam(name = "level") LogLevel level, @WebParam(name = "exception") Exception exception) {
		if (message == null && exception != null) {
			message = exception.getMessage();
			if (message == null) {
				message = "An unknown error has occured";
			}
		}
		if (message != null) {
			if (level == null) {
				level = exception == null ? LogLevel.INFO : LogLevel.ERROR;
			}
			if (loggerName == null) {
				ServiceRuntime runtime = ServiceRuntime.getRuntime().getParent();
				while (runtime != null) {
					Service unwrap = ServiceUtils.unwrap(runtime.getService());
					if (unwrap instanceof DefinedService) {
						loggerName = ((DefinedService) unwrap).getId();
						break;
					}
					runtime = runtime.getParent();
				}
			}
			Logger logger = loggerName == null ? this.logger : LoggerFactory.getLogger(loggerName);
			switch(level) {
				case DEBUG: 
					if (exception != null) {
						logger.debug(message, exception);
					}
					else {
						logger.debug(message);
					}
				break;
				case INFO: 
					if (exception != null) {
						logger.info(message, exception);
					}
					else {
						logger.info(message);
					}
				break;
				case WARN: 
					if (exception != null) {
						logger.warn(message, exception);
					}
					else {
						logger.warn(message);
					}
				break;
				case ERROR: 
					if (exception != null) {
						logger.error(message, exception);
					}
					else {
						logger.error(message);
					}
				break;
			}
		}
	}

	@ServiceDescription(comment = "Generate a new globally unique identifier")
	@WebResult(name = "uuid")
	@NotNull
	public UUID uuid() {
		return UUID.randomUUID();
	}
	
	@WebResult(name = "password")
	@NotNull
	public String password(@WebParam(name = "length") Integer length, @WebParam(name = "allowedCharacters") String chars) {
		if (length == null) {
			length = 8;
		}
		StringBuilder builder = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			if (chars == null || chars.isEmpty()) {
				int nextInt = random.nextInt(simplePasswordCharacters.length);
				builder.append(simplePasswordCharacters[nextInt]);
			}
			else {
				int nextInt = random.nextInt(chars.length());
				builder.append(chars.charAt(nextInt));
			}
		}
		return builder.toString();
	}
	
	@WebResult(name = "host")
	@NotNull
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
	
	@WebResult(name = "aliases")
	public java.util.List<String> getServerAliases() {
		return EAIResourceRepository.getInstance().getAliases();
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
	
	public void setProperty(@NotNull @WebParam(name = "key") java.lang.String key, @WebParam(name = "value") java.lang.String value) {
		System.setProperty(key, value);
	}
	
	public void bringOnline() {
		ServiceRunner runner = EAIResourceRepository.getInstance().getServiceRunner();
		if (runner instanceof be.nabu.eai.server.Server) {
			((be.nabu.eai.server.Server) runner).bringOnline();
		}
		else {
			throw new IllegalStateException("Can not find the server");
		}
	}
	public void bringOffline() {
		ServiceRunner runner = EAIResourceRepository.getInstance().getServiceRunner();
		if (runner instanceof be.nabu.eai.server.Server) {
			((be.nabu.eai.server.Server) runner).bringOffline();
		}
		else {
			throw new IllegalStateException("Can not find the server");
		}
	}
	@WebResult(name = "online")
	public boolean isOnline() {
		ServiceRunner runner = EAIResourceRepository.getInstance().getServiceRunner();
		if (runner instanceof be.nabu.eai.server.Server) {
			return !((be.nabu.eai.server.Server) runner).isOffline();
		}
		else {
			throw new IllegalStateException("Can not find the server");
		}
	}
}
