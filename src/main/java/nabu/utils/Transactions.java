package nabu.utils;

import java.util.UUID;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.libs.services.api.ExecutionContext;

@WebService
public class Transactions {
	
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
}
