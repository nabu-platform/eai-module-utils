package nabu.utils;

import java.lang.String;
import java.util.UUID;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	@WebResult(name = "uuid")
	public UUID uuid() {
		return UUID.randomUUID();
	}
	
	@WebResult(name = "host")
	public String getHostName() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostName();
	}
}
