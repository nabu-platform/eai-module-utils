/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package nabu.utils;

import java.io.InputStream;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.eai.repository.EAIExecutionContext;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.server.rest.ServerREST;
import be.nabu.libs.events.api.EventDispatcher;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.utils.cep.api.CommonEvent;
import be.nabu.utils.cep.api.ComplexEvent;
import be.nabu.utils.cep.api.EventSeverity;
import be.nabu.utils.cep.impl.CEPUtils;
import be.nabu.utils.cep.impl.ComplexEventImpl;

@WebService
public class Event {
	
	private ExecutionContext executionContext;
	
	@WebResult(name = "event")
	public java.lang.Object create(@WebParam(name = "event") ComplexEvent event) {
		return event;
	}
	
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
	
	// annotations are meant as short key/value pairs like for example contract numbers, client numbers etc which give more context to the event stream
	// the values here should not be long jsonified content, that should be sent in a separate event in the data attribute
	public void annotate(@WebParam(name = "artifactId") java.lang.String artifactId, @WebParam(name = "key") java.lang.String key, @WebParam(name = "value") java.lang.String value, @WebParam(name = "message") java.lang.String message, @WebParam(name = "description") java.lang.String description) {
		EventDispatcher complexEventDispatcher = EAIResourceRepository.getInstance().getComplexEventDispatcher();
		if (complexEventDispatcher != null) {
			if (artifactId == null) {
				ServiceRuntime parent = ServiceRuntime.getRuntime().getParent();
				if (parent != null && parent.getService() instanceof DefinedService) {
					artifactId = ((DefinedService) parent.getService()).getId();
				}
			}
			ComplexEventImpl complexEventImpl = new ComplexEventImpl();
			complexEventImpl.setCreated(new java.util.Date());
			complexEventImpl.setEventCategory("annotation");
			complexEventImpl.setEventName(key);
			complexEventImpl.setArtifactId(artifactId);
			complexEventImpl.setMessage(message);
			complexEventImpl.setReason(description);
			// at first we took "data" here, the thing is however you likely want to search on the value itself
			// indexing the data field is generally less useful, but the code field is almost certainly already indexed
			complexEventImpl.setCode(value);
			complexEventDispatcher.fire(complexEventImpl, this);
		}
	}
	
	// should implement: be.nabu.eai.server.api.EventHandler.handle
	public void subscribe(@WebParam(name = "serviceId") java.lang.String serviceId, @WebParam(name = "prioritySeverity") EventSeverity prioritySeverity) {
		if (executionContext.getServiceContext().getServiceRunner() instanceof be.nabu.eai.server.Server) {
			((be.nabu.eai.server.Server) executionContext.getServiceContext().getServiceRunner()).getProcessor().add(serviceId, prioritySeverity);
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
	
//	// toggle whether or not events should be fired by this runtime
//	public void toggleFire(@WebParam(name = "enableEventFiring") boolean enableEventFiring) {
//		if (executionContext instanceof EAIExecutionContext) {
//			((EAIExecutionContext) executionContext).setDisableEvents(!enableEventFiring);
//		}
//	}
}
