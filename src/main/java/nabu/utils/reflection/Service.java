package nabu.utils.reflection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.libs.authentication.api.Token;
import be.nabu.libs.services.DefinedServiceResolverFactory;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.ServiceException;
import be.nabu.libs.services.api.ServiceResult;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.binding.api.Window;
import be.nabu.libs.types.binding.xml.XMLBinding;
import be.nabu.libs.types.mask.MaskedContent;

@WebService
public class Service {
	
	ExecutionContext context;
	
	@WebResult(name = "typeInstance")
	public Object newServiceInput(@WebParam(name = "serviceId") String id, @WebParam(name = "content") InputStream content, @WebParam(name = "charset") Charset charset) throws IOException, ParseException {
		if (id == null) {
			return null;
		}
		DefinedService service = DefinedServiceResolverFactory.getInstance().getResolver().resolve(id);
		if (service == null) {
			throw new IllegalArgumentException("Service not found: " + id);
		}
		if (content != null) {
			XMLBinding binding = new XMLBinding(service.getServiceInterface().getInputDefinition(), charset == null ? Charset.defaultCharset() : charset);
			return binding.unmarshal(content, new Window[0]);
		}
		else {
			return service.getServiceInterface().getInputDefinition().newInstance();
		}
	}
	
	@SuppressWarnings("unchecked")
	@WebResult(name = "output")
	public Object invoke(@WebParam(name = "serviceId") String id, @WebParam(name = "input") Object input, @WebParam(name = "runAs") Token token) throws ServiceException, InterruptedException, ExecutionException {
		if (id == null) {
			return null;
		}
		DefinedService service = DefinedServiceResolverFactory.getInstance().getResolver().resolve(id);
		if (service == null) {
			throw new IllegalArgumentException("Service not found: " + id);
		}
		ComplexContent serviceInput;
		if (input != null) {
			if (!(input instanceof ComplexContent)) {
				input = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(input);
				if (input == null) {
					throw new IllegalArgumentException("Can not wrap input");
				}
			}
			if (((ComplexContent) input).getType().equals(service.getServiceInterface().getInputDefinition())) {
				serviceInput = (ComplexContent) input;
			}
			else {
				serviceInput = new MaskedContent((ComplexContent) input, service.getServiceInterface().getInputDefinition());
			}
		}
		else {
			serviceInput = service.getServiceInterface().getInputDefinition().newInstance();
		}
		ExecutionContext context = this.context;
		// currently you can't upgrade the security of the existing context
		// it is unclear if this is a good thing or a bad one
		// the added ability to set a token for an invoke() is interesting either way, whether we create a new context or update the existing one is irrelevant for the current usecase (task execution)
		// to be reevaluated if a new usecase comes along
		if (token != null) {
			context = EAIResourceRepository.getInstance().newExecutionContext(token);
		}
		// allow different targets or not? can set different target in invoke itself...?
		Future<ServiceResult> run = EAIResourceRepository.getInstance().getServiceRunner().run(service, context, serviceInput);
		ServiceResult serviceResult = run.get();
		if (serviceResult != null && serviceResult.getException() != null) {
			throw serviceResult.getException();
		}
		return serviceResult == null ? null : serviceResult.getOutput();
	}
}
