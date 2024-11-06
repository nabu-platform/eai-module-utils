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
	
	@WebResult(name = "transactionId")
	public String defaultTransactionId() {
		return executionContext.getTransactionContext().getDefaultTransactionId();
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
