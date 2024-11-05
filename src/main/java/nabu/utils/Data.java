package nabu.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.libs.types.binding.BindingProviderFactory;
import be.nabu.libs.types.binding.api.BindingProvider;
import be.nabu.libs.types.binding.api.DynamicBindingProvider;
import be.nabu.libs.types.binding.api.UnmarshallableBinding;
import be.nabu.libs.types.binding.api.Window;

@WebService
public class Data {
	@WebResult(name = "unmarshalled")
	public java.lang.Object unmarshalDynamic(@WebParam(name = "input") InputStream input, @NotNull @WebParam(name = "contentType") java.lang.String contentType, @WebParam(name = "charset") Charset charset) throws IOException, ParseException {
		// TODO: if content type is missing, make an educated guess based on the first byte, e.g. <, {, # ...
		BindingProvider provider = BindingProviderFactory.getInstance().getProviderFor(contentType);
		if (!(provider instanceof DynamicBindingProvider)) {
			throw new IllegalArgumentException("Could not find a dynamic binding provider for content type: " + contentType);
		}
		UnmarshallableBinding binding = ((DynamicBindingProvider) provider).getDynamicUnmarshallableBinding(charset == null ? Charset.defaultCharset() : charset);
		return binding.unmarshal(input, new Window[0]);
	}
}
