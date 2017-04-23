package nabu.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.lang.String;
import java.lang.Object;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.KeyValuePair;

@WebService
public class Properties {
	
	private ExecutionContext executionContext;
	
	@WebResult(name = "object")
	public Object toObject(@NotNull @WebParam(name = "typeId") String typeId, @WebParam(name = "properties") List<KeyValuePair> properties) {
		DefinedType resolved = executionContext.getServiceContext().getResolver(DefinedType.class).resolve(typeId);
		if (resolved == null) {
			throw new IllegalArgumentException("Could not find the type: " + typeId);
		}
		if (!(resolved instanceof ComplexType)) {
			throw new IllegalArgumentException("The resolved type is not complex: " + typeId);
		}
		ComplexContent newInstance = ((ComplexType) resolved).newInstance();
		if (properties != null) {
			for (KeyValuePair property : properties) {
				if (newInstance.getType().get(property.getKey()) != null) {
					newInstance.set(property.getKey().replace(".", "/"), property.getValue());
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
