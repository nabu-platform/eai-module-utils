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

package nabu.services;

import javax.jws.WebParam;

import be.nabu.libs.events.EventDispatcherFactory;
import be.nabu.libs.events.api.EventHandler;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.ServiceException;
import be.nabu.libs.types.api.ComplexContent;

public class Notifier {
	
	private ExecutionContext executionContext;
	private static Notifier instance = new Notifier();
	
	public void fire(@WebParam(name = "event") Object event) {
		EventDispatcherFactory.getInstance().getEventDispatcher().fire(event, instance);
	}
	
	public void subscribe(@WebParam(name = "serviceId") final String serviceToCall, @WebParam(name = "filter") final String filter) {
		EventDispatcherFactory.getInstance().getEventDispatcher().subscribe(Object.class, new NotificationEventHandler(serviceToCall), instance).filter(new NotificationEventFilter());
	}
	
	private final class NotificationEventFilter implements EventHandler<Object, Boolean> {
		@Override
		public Boolean handle(Object arg0) {
			// only complex content is allowed currently
			if (!(arg0 instanceof ComplexContent)) {
				return true;
			}
			// make sure the type matches the given filter criteria
			return false;
		}
	}

	private final class NotificationEventHandler implements EventHandler<Object, Object> {
		private final String serviceToCall;
		
		private NotificationEventHandler(String serviceToCall) {
			this.serviceToCall = serviceToCall;
		}
		
		@Override
		public Object handle(Object content) {
			DefinedService service = executionContext.getServiceContext().getResolver(DefinedService.class).resolve(serviceToCall);
			if (service == null) {
				throw new RuntimeException("The service " + serviceToCall + " can not be found");
			}
			// TODO: don't send it in as the full input pipeline, it is actually a single field on the pipeline
			// match them by type (or aspect)
			try {
				return new ServiceRuntime(service, executionContext).run((ComplexContent) content);
			}
			catch (ServiceException e) {
				return e;
			}
		}
	}
}
