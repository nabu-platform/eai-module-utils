package nabu.utils;

import java.io.InputStream;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.server.rest.ServerREST;
import be.nabu.libs.events.api.EventDispatcher;
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
		// if the execution context does not have any, we use the "standard" one
		// in some cases we explicitly unset the dispatcher in the execution context to prevent all the "standard" events from firing
		// this is generally because of extreme volume or because you are in the business of _processing_ events, which should not create new events to prevent loops
		// in general we send very few events from actual business code or even frameworks though, and so far never in the context of processing events
		// if, in the future we do want to send events from pieces of logic that do not know whether they are part of an event processing chain
		// we can make this configurable
		else {
			EventDispatcher complexEventDispatcher = EAIResourceRepository.getInstance().getComplexEventDispatcher();
			if (complexEventDispatcher != null) {
				complexEventDispatcher.fire(event, this);
			}
		}
	}
	
	// should implement: be.nabu.eai.server.api.EventHandler.handle
	public void subscribe(@WebParam(name = "serviceId") java.lang.String serviceId) {
		if (executionContext.getServiceContext().getServiceRunner() instanceof be.nabu.eai.server.Server) {
			((be.nabu.eai.server.Server) executionContext.getServiceContext().getServiceRunner()).getProcessor().add(serviceId);
		}
	}
	
	// should implement be.nabu.eai.repository.api.EventEnricher.enrich
	// not exposing this yet for performance considerations
//	public void enrich(@WebParam(name = "serviceId") java.lang.String serviceId) {
//		
//	}
	
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
