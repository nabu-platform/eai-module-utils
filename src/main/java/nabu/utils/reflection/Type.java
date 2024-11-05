package nabu.utils.reflection;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import nabu.utils.types.NodeDescription;
import nabu.utils.types.ParameterDescription;
import nabu.utils.types.TypeDescription;
import nabu.utils.types.TypeInspection;
import nabu.utils.types.TypeResult;

import java.lang.String;
import java.lang.Object;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.DefinedTypeResolverFactory;
import be.nabu.libs.types.SimpleTypeWrapperFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedSimpleType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.base.SimpleElementImpl;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.types.mask.MaskedContent;
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
	
	public void setAll(@WebParam(name = "values") List<TypeResult> results) {
		if (results != null && !results.isEmpty()) {
			for (TypeResult result : results) {
				if (result.getObject() != null) {
					ComplexContent content = result.getObject() instanceof ComplexContent ? ((ComplexContent) result.getObject()) : ComplexContentWrapperFactory.getInstance().getWrapper().wrap(result.getObject());
					if (content == null) {
						throw new IllegalArgumentException("Can not wrap objects of the type: " + result.getObject().getClass().getName());
					}
					content.set(result.getPath(), result.getValue());
				}
			}
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
	
	@WebResult(name = "values")
	public List<TypeResult> getAll(@WebParam(name = "typeInstances") List<Object> typeInstances, @NotNull @WebParam(name = "path") String path) {
		if (typeInstances == null || typeInstances.isEmpty()) {
			return null;
		}
		List<TypeResult> results = new ArrayList<TypeResult>();
		for (Object typeInstance : typeInstances) {
			ComplexContent content = typeInstance instanceof ComplexContent ? ((ComplexContent) typeInstance) : ComplexContentWrapperFactory.getInstance().getWrapper().wrap(typeInstance);
			if (content == null) {
				throw new IllegalArgumentException("Can not wrap objects of the type: " + typeInstance.getClass().getName());
			}
			results.add(new TypeResult(typeInstance, path, content.get(path)));
		}
		return results;
	}
	
	@WebResult(name = "description")
	public ParameterDescription details(@WebParam(name = "typeId") String id, @WebParam(name = "recursive") Boolean recursive) {
		if (id == null) {
			return null;
		}
		DefinedType type = DefinedTypeResolverFactory.getInstance().getResolver().resolve(id);
		if (type == null) {
			throw new IllegalArgumentException("Type not found: " + id);
		}
		return Node.describeType(type);
	}
	
	@WebResult(name = "parameters")
	public List<ParameterDescription> describe(@WebParam(name = "typeId") String id, @WebParam(name = "recursive") Boolean recursive) {
		if (id == null) {
			return null;
		}
		DefinedType type = DefinedTypeResolverFactory.getInstance().getResolver().resolve(id);
		if (type == null) {
			throw new IllegalArgumentException("Type not found: " + id);
		}
		return Node.toParameters((ComplexType) type, recursive != null && recursive);
	}
	
	@SuppressWarnings("unchecked")
	@WebResult(name = "inspection")
	public TypeInspection inspect(@WebParam(name = "object") Object object, @WebParam(name = "recursive") Boolean recursive) {
		if (object == null) {
			return null;
		}
		boolean isList = false;
		if (object instanceof Iterable) {
			isList = true;
			Iterator iterator = ((Iterable) object).iterator();
			if (!iterator.hasNext()) {
				return null;
			}
			object = iterator.next();
		}
		be.nabu.libs.types.api.Type resultingType;
		TypeInspection typeInspection = new TypeInspection();
		typeInspection.setList(isList);
		typeInspection.setHierarchy(new ArrayList<String>());
		DefinedSimpleType<? extends Object> wrap = SimpleTypeWrapperFactory.getInstance().getWrapper().wrap(object.getClass());
		if (wrap != null) {
			typeInspection.setSimple(true);
			typeInspection.setId(wrap.getId());
			typeInspection.setName(wrap.getName());
			resultingType = wrap;
		}
		else {
			if (!(object instanceof ComplexContent)) {
				object = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(object);
				if (object == null) {
					throw new IllegalArgumentException("This type of object is currently not supported");
				}
			}
			ComplexType type = ((ComplexContent) object).getType();
			resultingType = type;
			typeInspection.setParameters(Node.toParameters((ComplexType) type, recursive != null && recursive));
		}
		if (resultingType instanceof DefinedType) {
			typeInspection.setId(((DefinedType) resultingType).getId());
			typeInspection.setName(resultingType.getName());
			typeInspection.getHierarchy().add(((DefinedType) resultingType).getId());
		}
		while (resultingType.getSuperType() != null) {
			resultingType = resultingType.getSuperType();
			if (resultingType instanceof DefinedType) {
				typeInspection.getHierarchy().add(((DefinedType) resultingType).getId());	
			}
		}
		return typeInspection;
	}
	
	@SuppressWarnings("unchecked")
	@WebResult(name = "masked")
	public Object mask(@WebParam(name = "instance") Object instance, @WebParam(name = "type") String dataType) {
		if (instance == null) {
			return null;
		}
		else if (!(instance instanceof ComplexContent)) {
			instance = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(instance);
		}
		// no longer doing this, we might want to mask specifically to get retain the original as immutable
//		// check if we are already of the correct type
		ComplexType type = ((ComplexContent) instance).getType();
//		if (type instanceof DefinedType && ((DefinedType) type).getId().equals(dataType)) {
//			return instance;
//		}
//		Artifact resolve = EAIResourceRepository.getInstance().resolve(dataType);
		DefinedType resolve = DefinedTypeResolverFactory.getInstance().getResolver().resolve(dataType);
		if (!(resolve instanceof ComplexType)) {
			throw new IllegalArgumentException("Invalid type, expecting a complex type for: " + dataType);
		}
		return new MaskedContent((ComplexContent) instance, (ComplexType) resolve);	// type.equals(resolve) ? instance :
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
	
	@WebResult(name = "is")
	public boolean is(@WebParam(name = "typeInstance") Object typeInstance, @NotNull @WebParam(name = "typeId") String typeId) {
		if (typeInstance != null) {
			ComplexContent content = typeInstance instanceof ComplexContent ? ((ComplexContent) typeInstance) : ComplexContentWrapperFactory.getInstance().getWrapper().wrap(typeInstance);
			if (content != null) {
				ComplexType type = content.getType();
				while (type != null) {
					if (type instanceof DefinedType && ((DefinedType) type).getId().equals(typeId)) {
						return true;
					}
					type = (ComplexType) type.getSuperType();
				}
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@WebResult(name = "description")
	public TypeDescription whatIs(@WebParam(name = "object") Object object) {
		if (object == null) {
			return null;
		}
		boolean isList = false;
		if (object instanceof Iterable) {
			isList = true;
			Iterator iterator = ((Iterable) object).iterator();
			if (!iterator.hasNext()) {
				return null;
			}
			object = iterator.next();
		}
		TypeDescription description = new TypeDescription();
		description.setList(isList);
		DefinedSimpleType<? extends Object> wrap = SimpleTypeWrapperFactory.getInstance().getWrapper().wrap(object.getClass());
		if (wrap != null) {
			description.setSimple(true);
			description.setId(wrap.getId());
			description.setName(wrap.getName());
		}
		else {
			if (!(object instanceof ComplexContent)) {
				object = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(object);
			}
			if (object != null) {
				description.setComplex(true);
				ComplexType type = ((ComplexContent) object).getType();
				if (type instanceof DefinedType) {
					description.setId(((DefinedType) type).getId());
				}
			}
		}
		return description;
	}
	
	// currently unused (using object toValues instead)
	// and it seems to conflict with the glue "keys" method
	// not sure why the map.keys does not conflict?
//	@SuppressWarnings("unchecked")
	// by default it returns the list of parameters for this object that actually _have_ a value (could be null though)
//	public List<ParameterDescription> keys(Object object) {
//		if (object == null) {
//			return null;
//		}
//		ComplexContent content = object instanceof ComplexContent ? (ComplexContent) object : ComplexContentWrapperFactory.getInstance().getWrapper().wrap(object);
//		if (content == null) {
//			return null;
//		}
//		List<ParameterDescription> description = new ArrayList<ParameterDescription>();
//		for (Element<?> element : TypeUtils.getAllChildren(content.getType())) {
//			if (content.has(element.getName())) {
//				description.add(Node.describeType(element.getType(), element.getProperties()));
//			}
//		}
//		return description;
//	}
	
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
	
	@WebResult(name = "tags")
	public List<String> availableTags(@WebParam(name = "contextId") String contextId, @WebParam(name = "tags") List<String> mustHaveTags) {
		return Node.availableTags(contextId, mustHaveTags, DefinedType.class);
	}
	
	@WebResult(name = "types")
	public List<NodeDescription> listByTag(@WebParam(name = "contextId") String contextId, @NotNull @WebParam(name = "tags") List<String> mustHaveTags) {
		return Node.listByTag(contextId, mustHaveTags, DefinedType.class);
	}
}
