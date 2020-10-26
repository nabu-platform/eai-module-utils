package nabu.utils;

import java.io.InputStream;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.eai.server.rest.ServerREST;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.utils.cep.api.CommonEvent;
import be.nabu.utils.cep.impl.CEPUtils;

@WebService
public class Event {
	
	private ExecutionContext executionContext;
	
	public void fire(@WebParam(name = "event") java.lang.Object event) {
		if (executionContext.getEventTarget() != null) {
			executionContext.getEventTarget().fire(event, this);
		}
	}
	
	// should implement: be.nabu.eai.server.api.EventHandler.handle
	public void subscribe(@WebParam(name = "serviceId") java.lang.String serviceId) {
		if (executionContext.getServiceContext().getServiceRunner() instanceof be.nabu.eai.server.Server) {
			((be.nabu.eai.server.Server) executionContext.getServiceContext().getServiceRunner()).getProcessor().add(serviceId);
		}
	}
	
	public java.lang.String format(@WebParam(name = "events") java.util.List<java.lang.Object> events, @WebParam(name = "anonymize") Boolean anonymize) {
		StringBuilder builder = new StringBuilder();
		CEPUtils.asCEF(builder, "Nabu Platform", "Nabu Server", new ServerREST().getVersion(), anonymize != null && anonymize, events);
		return builder.toString();
	}
	
	@WebResult(name = "events")
	public java.util.List<CommonEvent> parse(@WebParam(name = "stream") InputStream input) {
		if (input != null) {
			return CEPUtils.parse(input);
		}
		return null;
	}
}
