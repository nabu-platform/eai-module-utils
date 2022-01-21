package nabu.utils;

import javax.jws.WebParam;
import javax.jws.WebService;

import be.nabu.libs.services.api.ExecutionContext;

@WebService
public class Metric {

	private ExecutionContext executionContext;
	
	// should implement: be.nabu.eai.server.api.MetricStatisticsHandler.handle
	public void subscribe(@WebParam(name = "serviceId") java.lang.String serviceId) {
		if (executionContext.getServiceContext().getServiceRunner() instanceof be.nabu.eai.server.Server) {
			((be.nabu.eai.server.Server) executionContext.getServiceContext().getServiceRunner()).getMetricsStatisticsProcessor().add(serviceId);
		}
	}
}
