package nabu.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.libs.types.BaseTypeInstance;
import be.nabu.libs.types.CollectionHandlerFactory;
import be.nabu.libs.types.DefinedTypeResolverFactory;
import be.nabu.libs.types.TypeConverterFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.CollectionHandlerProvider;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.DefinedTypeResolver;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.TypeConverter;
import be.nabu.libs.types.java.BeanInstance;

@WebService
public class Properties {
	
	private TypeConverter converter = TypeConverterFactory.getInstance().getConverter();
	private DefinedTypeResolver resolver = DefinedTypeResolverFactory.getInstance().getResolver();
	
	@WebResult(name = "object")
	public Object toObject(@WebParam(name = "typeId") String typeId, @WebParam(name = "properties") List<Property> properties) {
		DefinedType resolved = resolver.resolve(typeId);
		if (resolved == null) {
			throw new IllegalArgumentException("Could not find the type: " + typeId);
		}
		if (!(resolved instanceof ComplexType)) {
			throw new IllegalArgumentException("The resolved type is not complex: " + typeId);
		}
		ComplexContent newInstance = ((ComplexType) resolved).newInstance();
		if (properties != null) {
			for (Property property : properties) {
				newInstance.set(property.getKey(), property.getKey());
			}
		}
		return newInstance;
	}
	
	@WebResult(name = "properties")
	@SuppressWarnings("rawtypes")
	public List<Property> toProperties(@WebParam(name = "object") Object object) {
		List<Property> properties = new ArrayList<Property>();
		if (object != null) {
			ComplexContent content = object instanceof ComplexContent ? (ComplexContent) object : new BeanInstance(object);
			toProperties(content, properties, null);
		}
		return properties;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void toProperties(ComplexContent content, List<Property> properties, String path) {
		for (Element<?> child : TypeUtils.getAllChildren(content.getType())) {
			String childPath = path == null ? child.getName() : path + "." + child.getName();
			Object value = content.get(child.getName());
			if (value != null) {
				CollectionHandlerProvider collectionHandler = CollectionHandlerFactory.getInstance().getHandler().getHandler(value.getClass());
				if (collectionHandler != null) {
					java.lang.System.out.println("MARSHALLING: " + value + " > " + collectionHandler.getIndexes(value));
					for (Object index : collectionHandler.getIndexes(value)) {
						Object singleValue = collectionHandler.get(value, index);
						if (singleValue != null) {
							String singlePath = childPath;
							if (index instanceof Number) {
								singlePath += "[" + index + "]";
							}
							else {
								singlePath += "[\"" + index + "\"]";
							}
							singleToProperties(child, singleValue, properties, singlePath);
						}
					}
				}
				else {
					singleToProperties(child, value, properties, childPath);
				}
			}
		}
	}
	
	private void singleToProperties(Element<?> child, Object value, List<Property> properties, String childPath) {
		if (value instanceof ComplexContent) {
			toProperties((ComplexContent) value, properties, childPath);
		}
		else {
			properties.add(new Property(childPath, value instanceof String 
				? (String) value
				: (java.lang.String) converter.convert(value, child, new BaseTypeInstance(new be.nabu.libs.types.simple.String()))));
		}
	}
	
	/**
	 * TODO: currently most libraries are updated to allow for generics to be used here
	 * Normally the only change that still needs to occur is the merge of the types-evaluator logic with the evaluator-api
	 * The latter is massively updated with regards to collection handling
	 */
	@WebResult(name = "map")
	public Map<String, String> asMap(@WebParam(name = "properties") List<Property> properties) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		if (properties != null) {
			for (Property property : properties) {
				if (property != null) {
					map.put(property.getKey(), property.getValue());
				}
			}
		}
		return map;
	}
	
}
