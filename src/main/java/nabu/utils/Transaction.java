package nabu.utils;

import java.util.UUID;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.Transactionable;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.api.ComplexContent;

@WebService
public class Transaction {
	
	private ExecutionContext executionContext;
	
	@WebResult(name = "transactionId")
	public String start() {
		return UUID.randomUUID().toString();
	}
	
	public void commit(@WebParam(name = "transactionId") String transactionId) {
		executionContext.getTransactionContext().commit(transactionId);
	}
	
	public void rollback(@WebParam(name = "transactionId") String transactionId) {
		executionContext.getTransactionContext().rollback(transactionId);
	}
	
	// always add this to the top of the transaction context, otherwise things like jdbc adapters, streams etc might be closed
	public void onCommit(@WebParam(name = "transactionId") String transactionId, @NotNull @WebParam(name = "serviceId") String serviceId, @WebParam(name = "input") java.lang.Object input) {
		executionContext.getTransactionContext().push(transactionId, toTransactionable(serviceId, input, true, false));
	}
	
	public void onRollback(@WebParam(name = "transactionId") String transactionId, @NotNull @WebParam(name = "serviceId") String serviceId, @WebParam(name = "input") java.lang.Object input) {
		executionContext.getTransactionContext().push(transactionId, toTransactionable(serviceId, input, false, true));
	}
	
	@SuppressWarnings("unchecked")
	private Transactionable toTransactionable(final String serviceId, java.lang.Object input, final boolean commit, final boolean rollback) {
		final Artifact service = (DefinedService) EAIResourceRepository.getInstance().resolve(serviceId);
		if (!(service instanceof Service)) {
			throw new IllegalArgumentException("Invalid service id: " + serviceId);
		}
		final ComplexContent serviceInput = input == null ? null :
			(input instanceof ComplexContent ? (ComplexContent) input : ComplexContentWrapperFactory.getInstance().getWrapper().wrap(input));
		return new Transactionable() {
			private String id = UUID.randomUUID().toString().replace("-", "");
			@Override
			public void start() {
				// do nothing
			}
			@Override
			public void rollback() {
				if (rollback) {
					EAIResourceRepository.getInstance().getServiceRunner().run((Service) service, executionContext, serviceInput);
				}
			}
			@Override
			public String getId() {
				return id;
			}
			@Override
			public void commit() {
				if (commit) {
					EAIResourceRepository.getInstance().getServiceRunner().run((Service) service, executionContext, serviceInput);
				}
			}
		};
	}
}
