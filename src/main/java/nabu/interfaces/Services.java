package nabu.interfaces;

import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public interface Services {
	public void track(@WebParam(name="isBefore") boolean isBefore, @WebParam(name="serviceId") String service, @WebParam(name="step") String step, @WebParam(name="exception") Exception exception);
}
