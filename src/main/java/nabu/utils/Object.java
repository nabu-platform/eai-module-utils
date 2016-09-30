package nabu.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.io.ByteArrayInputStream;
import java.lang.String;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import nabu.utils.Date.ExtendedTimeUnit;
import nabu.utils.types.Property;
import be.nabu.libs.property.ValueUtils;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.BaseTypeInstance;
import be.nabu.libs.types.CollectionHandlerFactory;
import be.nabu.libs.types.DefinedTypeResolverFactory;
import be.nabu.libs.types.TypeConverterFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.CollectionHandlerProvider;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.KeyValuePair;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.api.Type;
import be.nabu.libs.types.api.TypeConverter;
import be.nabu.libs.types.java.BeanInstance;
import be.nabu.libs.types.properties.EnumerationProperty;
import be.nabu.libs.types.properties.MaxOccursProperty;

@WebService
public class Object {
	
	private TypeConverter converter = TypeConverterFactory.getInstance().getConverter();

	@WebResult(name = "properties")
	@SuppressWarnings("rawtypes")
	public List<KeyValuePair> toProperties(@WebParam(name = "object") java.lang.Object object) {
		List<KeyValuePair> properties = new ArrayList<KeyValuePair>();
		if (object != null) {
			ComplexContent content = object instanceof ComplexContent ? (ComplexContent) object : new BeanInstance(object);
			toProperties(content, properties, null);
		}
		return properties;
	}
	
	@WebResult(name = "duplicate")
	@SuppressWarnings("rawtypes")
	public java.lang.Object duplicate(@WebParam(name = "object") java.lang.Object object, @WebParam(name = "deep") Boolean deep) {
		if (object != null) {
			ComplexContent content = object instanceof ComplexContent ? (ComplexContent) object : new BeanInstance(object);
			return duplicateContent(content, deep != null && deep);
		}
		return null;
	}
	
	private ComplexContent duplicateContent(ComplexContent content, boolean deep) {
		ComplexContent duplicate = content.getType().newInstance();
		for (Element<?> child : TypeUtils.getAllChildren(duplicate.getType())) {
			java.lang.Object value = content.get(child.getName());
			if (value != null) {
				if (!deep) {
					duplicate.set(child.getName(), value);
				}
				// we need to check if it's a complex type as well, if so we need to recursively duplicate it
				// we also need to take care of lists and create new ones etc
				// alternatively we marshal & unmarshal the content, this would break streams etc but they are already in doubtful state when doing actual duplication
				else {
					throw new RuntimeException("Deep duplication is not yet supported");
				}
			}
		}
		return duplicate;
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
	private void toProperties(ComplexContent content, List<KeyValuePair> properties, String path) {
		for (Element<?> child : TypeUtils.getAllChildren(content.getType())) {
			String childPath = path == null ? child.getName() : path + "." + child.getName();
			java.lang.Object value = content.get(child.getName());
			if (value != null) {
				CollectionHandlerProvider collectionHandler = CollectionHandlerFactory.getInstance().getHandler().getHandler(value.getClass());
				if (collectionHandler != null) {
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
	
	private void singleToProperties(Element<?> child, java.lang.Object value, List<KeyValuePair> properties, String childPath) {
		if (value instanceof ComplexContent) {
			toProperties((ComplexContent) value, properties, childPath);
		}
		else {
			properties.add(new Property(childPath, value instanceof String 
				? (String) value
				: (java.lang.String) converter.convert(value, child, new BaseTypeInstance(new be.nabu.libs.types.simple.String()))));
		}
	}

	@WebResult(name = "stub")
	public java.lang.Object stub(@WebParam(name = "typeId") String id, @WebParam(name = "stubId") String identifier, @WebParam(name = "iterations") Integer amountOfIterations, @WebParam(name = "increment") ExtendedTimeUnit increment, @WebParam(name = "multiplier") Double multiplier) {
		if (id == null) {
			return null;
		}
		DefinedType type = DefinedTypeResolverFactory.getInstance().getResolver().resolve(id);
		if (type == null) {
			throw new IllegalArgumentException("Type not found: " + id);
		}
		if (type instanceof ComplexType) {
			Random random = new Random();
			if (identifier == null) {
				identifier = Integer.toString(random.nextInt());
			}
			ComplexContent content = ((ComplexType) type).newInstance();
			stub(content, identifier, amountOfIterations == null ? 1 : amountOfIterations, increment == null ? ExtendedTimeUnit.DAYS : increment, multiplier == null ? 1000 : multiplier, random);
			return content;
		}
		else if (type instanceof SimpleType) {
			throw new IllegalArgumentException("Can not instantiate a simple type");
		}
		else {
			return null;
		}
	}
	
	private void stub(ComplexContent content, String identifier, int amountOfIterations, ExtendedTimeUnit increment, double multiplier, Random random) {
		for (Element<?> element : TypeUtils.getAllChildren(content.getType())) {
			Type type = element.getType();
			java.lang.String path = element.getName();
			Value<Integer> maxOccurs = element.getProperty(MaxOccursProperty.getInstance());
			// if we have a list, set the path
			if (maxOccurs != null && maxOccurs.getValue() != null && !maxOccurs.getValue().equals(1)) {
				for (int i = 0; i < amountOfIterations; i++) {
					generateValue(content, identifier + "-" + i, amountOfIterations, increment, multiplier, element, type, path + "[" + i + "]", random, i);
				}
			}
			else {
				generateValue(content, identifier, amountOfIterations, increment, multiplier, element, type, path, random, 0);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void generateValue(ComplexContent content, String identifier, int amountOfIterations, ExtendedTimeUnit increment, double multiplier, Element<?> element, Type type, java.lang.String path, Random random, int iteration) {
		if (type instanceof ComplexType) {
			ComplexContent child = ((ComplexType) type).newInstance();
			stub(child, identifier, amountOfIterations, increment, multiplier, random);
			content.set(path, child);
		}
		else if (type instanceof SimpleType) {
			List values = (List) ValueUtils.getValue(new EnumerationProperty(), element.getProperties());
			if (values == null) {
				values = (List) ValueUtils.getValue(new EnumerationProperty(), element.getType().getProperties());
			}
			// if it is enumerated, take a random enumeration value
			if (values != null) {
				content.set(path, values.get(new Random().nextInt(values.size())));
			}
			else {
				Class<?> instanceClass = ((SimpleType<?>) type).getInstanceClass();
				if (Number.class.isAssignableFrom(instanceClass) || BigInteger.class.isAssignableFrom(instanceClass) || BigDecimal.class.isAssignableFrom(instanceClass)) {
					content.set(path, random.nextDouble() * multiplier);
				}
				else if (java.util.Date.class.isAssignableFrom(instanceClass)) {
					java.util.Date value = new java.util.Date();
					if (iteration > 0) {
						value = new nabu.utils.Date().increment(value, iteration, increment, TimeZone.getDefault());
					}
					content.set(path, value);
				}
				else if (java.lang.String.class.isAssignableFrom(instanceClass)) {
					content.set(path, element.getName() + "-" + identifier);
				}
				else if (java.lang.Boolean.class.isAssignableFrom(instanceClass)) {
					content.set(path, true);
				}
				else if (byte[].class.isAssignableFrom(instanceClass)) {
					content.set(path, (element.getName() + "-" + identifier).getBytes(Charset.forName("UTF-8")));
				}
				else if (java.io.InputStream.class.isAssignableFrom(instanceClass)) {
					content.set(path, new ByteArrayInputStream((element.getName() + "-" + identifier).getBytes(Charset.forName("UTF-8"))));
				}
				else if (UUID.class.isAssignableFrom(instanceClass)) {
					content.set(path, UUID.randomUUID());
				}
				else if (URI.class.isAssignableFrom(instanceClass)) {
					try {
						content.set(path, new URI("http://example.com/" + identifier));
					}
					catch (URISyntaxException e) {
						throw new RuntimeException(e);
					}
				}
				else if (TimeZone.class.isAssignableFrom(instanceClass)) {
					content.set(path, TimeZone.getDefault());
				}
				else if (Charset.class.isAssignableFrom(instanceClass)) {
					content.set(path, Charset.defaultCharset());
				}
			}
		}
	}
}
