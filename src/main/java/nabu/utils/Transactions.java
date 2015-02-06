package nabu.utils;

import java.util.UUID;
import java.lang.String;

import javax.jws.WebService;

import be.nabu.libs.services.api.ExecutionContext;

@WebService
public class Transactions {
	
	private ExecutionContext executionContext;
	
	public String start() {
		return UUID.randomUUID().toString();
	}
	
	public void commit(String transactionId) {
		executionContext.getTransactionContext().commit(transactionId);
	}
	
	public void rollback(String transactionId) {
		executionContext.getTransactionContext().rollback(transactionId);
	}
}
