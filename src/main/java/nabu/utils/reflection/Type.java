package nabu.utils.reflection;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import java.lang.String;
import java.lang.Object;

import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.DefinedTypeResolverFactory;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.SimpleType;

@WebService
public class Type {
	
	@WebResult(name = "typeInstance")
	public Object newInstance(@WebParam(name = "typeId") String id) {
		if (id != null) {
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
}
