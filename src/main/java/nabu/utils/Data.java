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
