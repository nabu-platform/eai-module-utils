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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.lang.String;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.Object;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.DefinedTypeResolverFactory;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.KeyValuePair;
import be.nabu.libs.types.base.TypeBaseUtils;
import be.nabu.libs.types.binding.api.Window;
import be.nabu.libs.types.binding.json.JSONBinding;
import be.nabu.libs.types.structure.Structure;

@WebService
public class Properties {
	
	private ExecutionContext executionContext;
	
	@SuppressWarnings("unchecked")
	@WebResult(name = "changed")
	public boolean mapToObject(@NotNull @WebParam(name = "into") Object target, @WebParam(name = "properties") List<KeyValuePair> properties) {
		boolean changed = false;
		ComplexContent targetContent = target instanceof ComplexContent ? (ComplexContent) target : ComplexContentWrapperFactory.getInstance().getWrapper().wrap(target);
		if (properties != null) {
			for (KeyValuePair pair : properties) {
				if (targetContent.getType().get(pair.getKey()) != null) {
					targetContent.set(pair.getKey(), pair.getValue());
					changed = true;
				}
			}
		}
		return changed;
	}
	
	@WebResult(name = "object")
	public Object toObject(@NotNull @WebParam(name = "typeId") String typeId, @WebParam(name = "properties") List<KeyValuePair> properties, @WebParam(name = "separator") String separator) {
		DefinedType resolved = executionContext.getServiceContext().getResolver(DefinedType.class).resolve(typeId);
		if (resolved == null) {
			resolved = DefinedTypeResolverFactory.getInstance().getResolver().resolve(typeId);
			if (resolved == null) {
				throw new IllegalArgumentException("Could not find the type: " + typeId);
			}
		}
		if (!(resolved instanceof ComplexType)) {
			throw new IllegalArgumentException("The resolved type is not complex: " + typeId);
		}
		ComplexContent newInstance = ((ComplexType) resolved).newInstance();
		if (properties != null) {
			for (KeyValuePair property : properties) {
				String key = property.getKey();
				if (key == null || key.equals("$all")) {
					java.lang.String value = property.getValue();
					if (value != null) {
						JSONBinding binding = new JSONBinding((ComplexType) resolved, Charset.forName("UTF-8"));
						binding.setIgnoreRootIfArrayWrapper(true);
						// TODO: array support necessary?
						try {
							// updating the instance as a whole or merging every key (even nulls) amounts to the same thing?
							newInstance = binding.unmarshal(new ByteArrayInputStream(value.getBytes(Charset.forName("UTF-8"))), new Window[0]);
						}
						catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}
				else {
					if (separator == null || !separator.equals("/")) {
						key = key.replace(separator == null ? "." : separator, "/");
					}
					boolean set = false;
					Element<?> element = newInstance.getType().get(key);
					if (element != null) {
						if (element.getType() instanceof ComplexType) {
							java.lang.String value = property.getValue();
							if (value != null) {
								ComplexType typeToUse = (ComplexType) element.getType();
								if (element.getType().isList(element.getProperties())) {
									Structure listWrapper = new Structure();
									listWrapper.setName("listWrapper");
									listWrapper.add(TypeBaseUtils.clone(element, listWrapper));
									typeToUse = listWrapper;
								}
								JSONBinding binding = new JSONBinding(typeToUse, Charset.forName("UTF-8"));
								binding.setIgnoreRootIfArrayWrapper(true);
								try {
									ComplexContent unmarshal = binding.unmarshal(new ByteArrayInputStream(value.getBytes(Charset.forName("UTF-8"))), new Window[0]);
									// updating the instance as a whole or merging every key (even nulls) amounts to the same thing?
									newInstance.set(key, element.getType().equals(typeToUse) ? unmarshal : unmarshal.get(element.getName()));
									set = true;
								}
								catch (Exception e) {
									throw new RuntimeException(e);
								}
							}
						}
						if (!set) {
							newInstance.set(key, property.getValue());
						}
					}
				}
			}
		}
		return newInstance;
	}
	
	@WebResult(name = "value")
	public String getValue(@NotNull @WebParam(name = "key") String key, @WebParam(name = "properties") List<KeyValuePair> properties) {
		for (KeyValuePair property : properties) {
			if (property != null && key.equals(property.getKey())) {
				return property.getValue();
			}
		}
		return null;
	}
	
	/**
	 * TODO: currently most libraries are updated to allow for generics to be used here
	 * Normally the only change that still needs to occur is the merge of the types-evaluator logic with the evaluator-api
	 * The latter is massively updated with regards to collection handling
	 */
	@WebResult(name = "map")
	public Map<String, String> toMap(@WebParam(name = "properties") List<KeyValuePair> properties) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		if (properties != null) {
			for (KeyValuePair property : properties) {
				if (property != null) {
					map.put(property.getKey(), property.getValue());
				}
			}
		}
		return map;
	}
	
}
