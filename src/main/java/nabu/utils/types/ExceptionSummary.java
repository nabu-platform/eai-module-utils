package nabu.utils.types;

import java.io.PrintWriter;
import java.io.StringWriter;

import be.nabu.libs.services.api.ServiceException;

public class ExceptionSummary {
	
	private String code, message, exceptionStack, serviceStack;

	public static ExceptionSummary build(Exception exception) {
		ExceptionSummary summary = new ExceptionSummary();
		Throwable current = exception;
		summary.setMessage(current.getMessage());
		boolean firstServiceStack = true;
		while (current != null) {
			if (current instanceof ServiceException) {
				// we want the details of the inner most service exception (except the service stack)
				// the code and message are more likely the actual cause
				summary.setMessage(current.getMessage());
				summary.setCode(((ServiceException) current).getCode());
				if (firstServiceStack && ((ServiceException) current).getServiceStack() != null && !((ServiceException) current).getServiceStack().isEmpty()) {
					summary.setServiceStack(((ServiceException) current).getServiceStack().toString());
					firstServiceStack = false;
				}
			}
			current = current.getCause();
		}
		StringWriter writer = new StringWriter();
		PrintWriter printer = new PrintWriter(writer);
		exception.printStackTrace(printer);
		printer.flush();
		summary.setExceptionStack(writer.toString());
		return summary;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getExceptionStack() {
		return exceptionStack;
	}

	public void setExceptionStack(String exceptionStack) {
		this.exceptionStack = exceptionStack;
	}

	public String getServiceStack() {
		return serviceStack;
	}

	public void setServiceStack(String serviceStack) {
		this.serviceStack = serviceStack;
	}
	
}
