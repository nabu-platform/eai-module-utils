package nabu.services;

import javax.jws.WebService;

import be.nabu.libs.events.EventDispatcherFactory;
import be.nabu.libs.events.api.EventHandler;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.ServiceException;
import be.nabu.libs.types.api.ComplexContent;

@WebService
public class Notifier {
	
	private ExecutionContext executionContext;
	private static Notifier instance = new Notifier();
	
	public void fire(Object event) {
		EventDispatcherFactory.getInstance().getEventDispatcher().fire(event, instance);
	}
	
	public void subscribe(final String serviceToCall, final String filter) {
		EventDispatcherFactory.getInstance().getEventDispatcher().subscribe(Object.class, new EventHandler<Object, Object>() {
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
			
		}, instance).filter(new EventHandler<Object, Boolean>() {
			@Override
			public Boolean handle(Object arg0) {
				// only complex content is allowed currently
				if (!(arg0 instanceof ComplexContent)) {
					return true;
				}
				// make sure the type matches the given filter criteria
				return false;
			}
		});
	}
}
