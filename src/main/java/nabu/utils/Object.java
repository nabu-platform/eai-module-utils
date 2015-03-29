package nabu.utils;

import java.util.ArrayList;
import java.util.List;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.libs.types.BaseTypeInstance;
import be.nabu.libs.types.CollectionHandlerFactory;
import be.nabu.libs.types.TypeConverterFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.CollectionHandlerProvider;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.TypeConverter;
import be.nabu.libs.types.java.BeanInstance;

@WebService
public class Object {
	
	private TypeConverter converter = TypeConverterFactory.getInstance().getConverter();

	@WebResult(name = "properties")
	@SuppressWarnings("rawtypes")
	public List<Property> toProperties(@WebParam(name = "object") java.lang.Object object) {
		List<Property> properties = new ArrayList<Property>();
		if (object != null) {
			ComplexContent content = object instanceof ComplexContent ? (ComplexContent) object : new BeanInstance(object);
			toProperties(content, properties, null);
		}
		return properties;
	}
	
	@WebResult(name = "first")
	public java.lang.Object first(@WebParam(name = "options") List<java.lang.Object> options) {
		if (options != null) {
			for (java.lang.Object object : options) {
				if (object != null) {
					return object;
				}
			}
		}
		return null;
	}
	
	@WebResult(name = "last")
	public java.lang.Object last(@WebParam(name = "options") List<java.lang.Object> options) {
		java.lang.Object last = null;
		if (options != null) {
			for (java.lang.Object object : options) {
				if (object != null) {
					last = object;
				}
			}
		}
		return last;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void toProperties(ComplexContent content, List<Property> properties, String path) {
		for (Element<?> child : TypeUtils.getAllChildren(content.getType())) {
			String childPath = path == null ? child.getName() : path + "." + child.getName();
			java.lang.Object value = content.get(child.getName());
			if (value != null) {
				CollectionHandlerProvider collectionHandler = CollectionHandlerFactory.getInstance().getHandler().getHandler(value.getClass());
				if (collectionHandler != null) {
					java.lang.System.out.println("MARSHALLING: " + value + " > " + collectionHandler.getIndexes(value));
					for (java.lang.Object index : collectionHandler.getIndexes(value)) {
						java.lang.Object singleValue = collectionHandler.get(value, index);
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
	
	private void singleToProperties(Element<?> child, java.lang.Object value, List<Property> properties, String childPath) {
		if (value instanceof ComplexContent) {
			toProperties((ComplexContent) value, properties, childPath);
		}
		else {
			properties.add(new Property(childPath, value instanceof String 
				? (String) value
				: (java.lang.String) converter.convert(value, child, new BaseTypeInstance(new be.nabu.libs.types.simple.String()))));
		}
	}

}
