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
import be.nabu.libs.types.api.SimpleType;

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
}
