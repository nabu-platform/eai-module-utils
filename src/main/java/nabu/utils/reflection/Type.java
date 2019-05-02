package nabu.utils.reflection;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import nabu.utils.types.ParameterDescription;

import java.lang.String;
import java.lang.Object;
import java.util.List;

import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.DefinedTypeResolverFactory;
import be.nabu.libs.types.SimpleTypeWrapperFactory;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedSimpleType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.base.SimpleElementImpl;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.types.properties.MaxInclusiveProperty;
import be.nabu.libs.types.properties.MaxLengthProperty;
import be.nabu.libs.types.properties.MaxOccursProperty;
import be.nabu.libs.types.properties.MinInclusiveProperty;
import be.nabu.libs.types.properties.MinLengthProperty;
import be.nabu.libs.types.properties.MinOccursProperty;
import be.nabu.libs.types.properties.PatternProperty;
import be.nabu.libs.types.structure.Structure;

@WebService
public class Type {
	
	//public List<ValidationMessage> validateProperties(@WebParam(name = "properties") List<KeyValuePair> properties)
	
	@WebResult(name = "typeInstance")
	public Object newInstance(@WebParam(name = "typeId") String id) {
		if (id == null) {
			return null;
		}
		DefinedType type = DefinedTypeResolverFactory.getInstance().getResolver().resolve(id);
		if (type == null) {
			throw new IllegalArgumentException("Type not found: " + id);
		}
		if (type instanceof ComplexType) {
			return ((ComplexType) type).newInstance();
		}
		else if (type instanceof SimpleType) {
			throw new IllegalArgumentException("Can not instantiate a simple type");
		}
		else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void set(@WebParam(name = "typeInstance") Object typeInstance, @NotNull @WebParam(name = "path") String path, @WebParam(name = "value") Object value) {
		if (typeInstance != null) {
			ComplexContent content = typeInstance instanceof ComplexContent ? ((ComplexContent) typeInstance) : ComplexContentWrapperFactory.getInstance().getWrapper().wrap(typeInstance);
			if (content == null) {
				throw new IllegalArgumentException("Can not wrap objects of the type: " + typeInstance.getClass().getName());
			}
			content.set(path, value);
		}
	}
	
	@SuppressWarnings("unchecked")
	@WebResult(name = "value")
	public Object get(@WebParam(name = "typeInstance") Object typeInstance, @NotNull @WebParam(name = "path") String path) {
		if (typeInstance != null) {
			ComplexContent content = typeInstance instanceof ComplexContent ? ((ComplexContent) typeInstance) : ComplexContentWrapperFactory.getInstance().getWrapper().wrap(typeInstance);
			if (content == null) {
				throw new IllegalArgumentException("Can not wrap objects of the type: " + typeInstance.getClass().getName());
			}
			return content.get(path);
		}
		return null;
	}
	
	@WebResult(name = "parameters")
	public List<ParameterDescription> describe(@WebParam(name = "typeId") String id) {
		if (id == null) {
			return null;
		}
		DefinedType type = DefinedTypeResolverFactory.getInstance().getResolver().resolve(id);
		if (type == null) {
			throw new IllegalArgumentException("Type not found: " + id);
		}
		return Node.toParameters((ComplexType) type);
	}
	
	@SuppressWarnings("unchecked")
	@WebResult(name = "typeId")
	public String of(@WebParam(name = "typeInstance") Object typeInstance) {
		if (typeInstance != null) {
			DefinedSimpleType<? extends Object> wrap = SimpleTypeWrapperFactory.getInstance().getWrapper().wrap(typeInstance.getClass());
			if (wrap != null) {
				return wrap.getId();
			}
			ComplexContent content = typeInstance instanceof ComplexContent ? ((ComplexContent) typeInstance) : ComplexContentWrapperFactory.getInstance().getWrapper().wrap(typeInstance);
			if (content != null && content.getType() instanceof DefinedType) {
				return ((DefinedType) content.getType()).getId();
			}
		}
		return null;
	}
	// maybe expose this at some point in the future
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@WebResult(name = "type")
	private Object toType(@WebParam(name = "parameters") List<ParameterDescription> parameters) {
		Structure structure = new Structure();
		structure.setName("anonymous");
		if (parameters != null) {
			for (ParameterDescription description : parameters) {
				SimpleType<?> type = null;
				if (description.getType() != null) {
					type = (SimpleType<?>) DefinedTypeResolverFactory.getInstance().getResolver().resolve(description.getType());
				}
				else if (description.getTypeName() != null) {
					type = SimpleTypeWrapperFactory.getInstance().getWrapper().getByName(description.getTypeName());
				}
				if (type == null) {
					type = SimpleTypeWrapperFactory.getInstance().getWrapper().wrap(String.class);
				}
				Element<?> element = new SimpleElementImpl(description.getName(), type, structure);
				if (description.isOptional()) {
					element.setProperty(new ValueImpl<Integer>(MinOccursProperty.getInstance(), 0));
				}
				if (description.isList()) {
					element.setProperty(new ValueImpl<Integer>(MaxOccursProperty.getInstance(), 0));
				}
				if (String.class.isAssignableFrom(type.getInstanceClass())) {
					if (description.getMaximum() != null) {
						element.setProperty(new ValueImpl<Integer>(MaxLengthProperty.getInstance(), description.getMaximum()));
					}
					if (description.getMinimum() != null) {
						element.setProperty(new ValueImpl<Integer>(MinLengthProperty.getInstance(), description.getMinimum()));
					}
				}
				else if (Number.class.isAssignableFrom(type.getInstanceClass())) {
					if (description.getMaximum() != null) {
						element.setProperty(new ValueImpl<Integer>(new MinInclusiveProperty<Integer>(), description.getMaximum()));
					}
					if (description.getMinimum() != null) {
						element.setProperty(new ValueImpl<Integer>(new MaxInclusiveProperty<Integer>(), description.getMinimum()));
					}
				}
				if (description.getPattern() != null) {
					element.setProperty(new ValueImpl<String>(PatternProperty.getInstance(), description.getPattern()));
				}
				structure.add(element);
			}
		}
		return structure;
	}
	
	@WebResult(name = "typeInstance")
	public Object toTypeInstance(@WebParam(name = "parameters") List<ParameterDescription> parameters) {
		ComplexType type = (ComplexType) toType(parameters);
		return type.newInstance();
	}
}
