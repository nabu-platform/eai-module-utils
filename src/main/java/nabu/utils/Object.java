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
import be.nabu.libs.services.api.ServiceDescription;
import be.nabu.libs.types.BaseTypeInstance;
import be.nabu.libs.types.CollectionHandlerFactory;
import be.nabu.libs.types.ComplexContentWrapperFactory;
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
import be.nabu.libs.types.mask.MaskedContent;
import be.nabu.libs.types.properties.EnumerationProperty;
import be.nabu.libs.types.properties.IdentifiableProperty;
import be.nabu.libs.types.properties.MaxOccursProperty;
import be.nabu.libs.types.properties.MinOccursProperty;
import be.nabu.libs.validator.api.Validation;
import be.nabu.libs.validator.api.Validator;

@WebService
public class Object {
	
	private TypeConverter converter = TypeConverterFactory.getInstance().getConverter();

	@ServiceDescription(comment = "Anonymize the data in an object")
	// anonimize an object
	@SuppressWarnings("unchecked")
	@WebResult(name = "anonymized")
	public java.lang.Object anonymize(@WebParam(name = "object") java.lang.Object object) {
		if (object == null) {
			return object;
		}
		else if (!(object instanceof ComplexContent)) {
			object = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(object);
			if (object == null) {
				return object;
			}
		}
		// we do a masked one so we can do changes without impacting the original object, it is more or less a cheap but effective clone
		ComplexContent anonymized = new MaskedContent((ComplexContent) object, ((ComplexContent) object).getType());
		anonymize(anonymized);
		return anonymized;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void anonymize(ComplexContent content) {
		for (Element<?> child : TypeUtils.getAllChildren(content.getType())) {
			java.lang.Object object = content.get(child.getName());
			if (object != null) {
				Value<Boolean> property = child.getProperty(IdentifiableProperty.getInstance());
				// if identifiable...let's anonimize it
				if (property != null && property.getValue() != null && property.getValue()) {
					if (child.getType().isList(child.getProperties())) {
						CollectionHandlerProvider handler = CollectionHandlerFactory.getInstance().getHandler().getHandler(object.getClass());
						// should be done with indexes etc, but...is slower and has very few usecases
						List list = new ArrayList();
	//					java.lang.Object list = handler.create(object.getClass(), 0);
						// if we need to anonimize entire complex contents, we just leave it empty, so we have an empty list
						for (java.lang.Object single : handler.getAsIterable(object)) {
							if (child.getType() instanceof SimpleType) {
								list.add(anonymizeSimple(child, single));
							}
						}
						content.set(child.getName(), list);
					}
					else if (child.getType() instanceof SimpleType) {
						content.set(child.getName(), anonymizeSimple(child, object));
					}
					// if we need to anonimize an entire object, we just set it to null
					else {
						content.set(child.getName(), null);
					}
				}
				// if the child itself does not have to be anonimized but it is a complex type, recurse
				else if (child.getType() instanceof ComplexType) {
					if (child.getType().isList(child.getProperties())) {
						CollectionHandlerProvider handler = CollectionHandlerFactory.getInstance().getHandler().getHandler(object.getClass());
						// should be done with indexes etc, but...is slower and has very few usecases
						List list = new ArrayList();
						for (java.lang.Object single : handler.getAsIterable(object)) {
							if (!(single instanceof ComplexContent)) {
								single = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(single);
							}
							if (single != null) {
								anonymize((ComplexContent) single);
							}
							list.add(single);
						}
						content.set(child.getName(), list);
					}
					else {
						if (!(object instanceof ComplexContent)) {
							object = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(object);
						}
						if (object != null) {
							anonymize(object);
						}
						content.set(child.getName(), object);
					}
				}
			}
		}
	}
	
	private java.lang.Object anonymizeSimple(Element<?> element, java.lang.Object simple) {
		Value<Integer> minOccurs = element.getProperty(MinOccursProperty.getInstance());
		// if it is optional, we don't even try to anonimize it
		if (minOccurs != null && minOccurs.getValue() != null && minOccurs.getValue() == 0) {
			return null;
		}
		SimpleType<?> type = (SimpleType<?>) element.getType();
		// any number is reset to 0
		if (Number.class.isAssignableFrom(type.getInstanceClass())) {
			simple = 0;
		}
		else if (UUID.class.isAssignableFrom(type.getInstanceClass()) || String.class.isAssignableFrom(type.getInstanceClass())) {
			simple = UUID.randomUUID();
		}
		else if (Date.class.isAssignableFrom(type.getInstanceClass())) {
			simple = new Date();
		}
		else if (Boolean.class.isAssignableFrom(type.getInstanceClass())) {
			simple = false;
		}
		// any other data type does not have proper anonimization routines yet, let's throw an exception
		else {
			throw new IllegalArgumentException("Can not anonimize type: " + type.getInstanceClass());
		}
		return simple;
	}
	
	@ServiceDescription(comment = "Validate the content of an object")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@WebResult(name = "validations")
	public List<Validation<?>> validate(@WebParam(name = "object") java.lang.Object object) {
		if (object == null) {
			return null;
		}
		else if (!(object instanceof ComplexContent)) {
			object = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(object);
		}
		Validator validator = ((ComplexContent) object).getType().createValidator();
		return validator.validate(object);
	}
	
	@ServiceDescription(comment = "Format an object into a string")
	@WebResult(name = "string")
	public String toString(@WebParam(name = "object") java.lang.Object object) {
		return object == null ? null : object.toString();
	}
	
	@ServiceDescription(comment = "Transform an object into key/value pairs")
	@WebResult(name = "properties")
	@SuppressWarnings("rawtypes")
	public List<KeyValuePair> toProperties(@WebParam(name = "object") java.lang.Object object, @WebParam(name = "separator") java.lang.String separator) {
		List<KeyValuePair> properties = new ArrayList<KeyValuePair>();
		if (object != null) {
			ComplexContent content = object instanceof ComplexContent ? (ComplexContent) object : new BeanInstance(object);
			toProperties(content, properties, null, separator == null ? "." : separator);
		}
		return properties;
	}

	@SuppressWarnings("unchecked")
	@WebResult(name = "changed")
	public boolean mapProperties(@WebParam(name = "into") java.lang.Object target, @WebParam(name = "properties") List<KeyValuePair> properties) {
		if (target == null || properties == null) {
			return false;
		}
		boolean set = false;
		ComplexContent targetContent = target instanceof ComplexContent ? (ComplexContent) target : ComplexContentWrapperFactory.getInstance().getWrapper().wrap(target);
		for (KeyValuePair property : properties) {
			if (targetContent.getType().get(property.getKey()) != null) {
				targetContent.set(property.getKey(), property.getValue());
				set = true;
			}
		}
		return set;
	}
	
	@ServiceDescription(comment = "Map the fields in an object by name")
	@SuppressWarnings("unchecked")
	@WebResult(name = "changed")
	public boolean mapByKey(@WebParam(name = "from") java.lang.Object source, @WebParam(name = "into") java.lang.Object target, @WebParam(name = "includeNull") java.lang.Boolean includeNull, @WebParam(name = "ignoredFields") List<java.lang.String> ignoredFields) {
		if (target == null || source == null) {
			return false;
		}
		if (includeNull == null) {
			includeNull = true;
		}
		ComplexContent sourceContent = source instanceof ComplexContent ? (ComplexContent) source : ComplexContentWrapperFactory.getInstance().getWrapper().wrap(source);
		ComplexContent targetContent = target instanceof ComplexContent ? (ComplexContent) target : ComplexContentWrapperFactory.getInstance().getWrapper().wrap(target);
	
		boolean changed = false;
		for (Element<?> element : TypeUtils.getAllChildren(targetContent.getType())) {
			Element<?> sourceElement = sourceContent.getType().get(element.getName());
			if (sourceElement != null && (ignoredFields == null || !ignoredFields.contains(element.getName()))) {
				java.lang.Object newValue = sourceContent.get(element.getName());
				if (newValue != null || includeNull || element.getType().isList(element.getProperties())) {
					java.lang.Object oldValue = targetContent.get(element.getName());
					if (oldValue == null || element.getType() instanceof SimpleType) {
						// this will make sure the conversions that are necessary have been applied
						java.lang.Object setNewValue = targetContent.get(element.getName());
						targetContent.set(element.getName(), newValue);
						// check if we actually changed the new value
						if ((newValue == null && setNewValue != null) || (newValue != null && !newValue.equals(setNewValue))) {
							changed = true;
						}
					}
					else {
						changed |= mapByKey(newValue, oldValue, includeNull, ignoredFields);
					}
				}
			}
		}
		return changed;
	}
	
	@ServiceDescription(comment = "Clone an object")
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

	@ServiceDescription(comment = "Get the first non-null option in a list")
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
	
	@ServiceDescription(comment = "Get the last non-null option in a list")
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
	private void toProperties(ComplexContent content, List<KeyValuePair> properties, String path, String separator) {
		for (Element<?> child : TypeUtils.getAllChildren(content.getType())) {
			String childPath = path == null ? child.getName() : path + separator + child.getName();
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
							singleToProperties(child, singleValue, properties, singlePath, separator);
						}
					}
				}
				else {
					singleToProperties(child, value, properties, childPath, separator);
				}
			}
		}
	}
	
	private void singleToProperties(Element<?> child, java.lang.Object value, List<KeyValuePair> properties, String childPath, String separator) {
		if (value instanceof ComplexContent) {
			toProperties((ComplexContent) value, properties, childPath, separator);
		}
		else {
			properties.add(new Property(childPath, value instanceof String 
				? (String) value
				: (java.lang.String) converter.convert(value, child, new BaseTypeInstance(new be.nabu.libs.types.simple.String()))));
		}
	}

	@ServiceDescription(comment = "Generate stub data")
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
			stub(content, identifier, amountOfIterations == null ? 1 : amountOfIterations, increment == null ? ExtendedTimeUnit.DAYS : increment, multiplier == null ? 1000 : multiplier, random, 0);
			return content;
		}
		else if (type instanceof SimpleType) {
			throw new IllegalArgumentException("Can not instantiate a simple type");
		}
		else {
			return null;
		}
	}
	
	private void stub(ComplexContent content, String identifier, int amountOfIterations, ExtendedTimeUnit increment, double multiplier, Random random, int iteration) {
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
				generateValue(content, identifier, amountOfIterations, increment, multiplier, element, type, path, random, iteration);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void generateValue(ComplexContent content, String identifier, int amountOfIterations, ExtendedTimeUnit increment, double multiplier, Element<?> element, Type type, java.lang.String path, Random random, int iteration) {
		if (type instanceof ComplexType) {
			ComplexContent child = ((ComplexType) type).newInstance();
			stub(child, identifier, amountOfIterations, increment, multiplier, random, iteration);
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
