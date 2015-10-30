package nabu.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.pojo.POJOUtils;

@WebService
public class List {
	
	private ExecutionContext executionContext;
	
	@WebResult(name = "contains")
	public boolean contains(@WebParam(name = "list") java.util.List<java.lang.Object> list, @WebParam(name = "object") java.lang.Object object) {
		return list == null || list.indexOf(object) < 0 ? false : true;
	}
	
	@WebResult(name = "index")
	public Integer indexOf(@WebParam(name = "list") java.util.List<java.lang.Object> list, @WebParam(name = "object") java.lang.Object object) {
		if (list == null) {
			return null;
		}
		int index = list.indexOf(object);
		return index < 0 ? null : index;
	}
	
	@WebResult(name = "list")
	public java.util.List<java.lang.Object> add(@WebParam(name = "list") java.util.List<java.lang.Object> list, @WebParam(name = "index") Integer index, @WebParam(name = "object") java.lang.Object object) {
		if (list == null) {
			list = new ArrayList<java.lang.Object>();
		}
		if (index == null) {
			list.add(object);
		}
		else {
			list.add(index, object);
		}
		return list;
	}
	
	@WebResult(name = "list")
	public java.lang.Object get(@WebParam(name = "list") java.util.List<java.lang.Object> list, @NotNull @WebParam(name = "index") Integer index) {
		if (list == null) {
			list = new ArrayList<java.lang.Object>();
		}
		return list.get(index);
	}

	@WebResult(name = "list")
	public java.util.List<java.lang.Object> set(@WebParam(name = "list") java.util.List<java.lang.Object> list, @NotNull @WebParam(name = "index") Integer index, @WebParam(name = "object") java.lang.Object object) {
		if (list == null) {
			list = new ArrayList<java.lang.Object>();
		}
		list.set(index, object);
		return list;
	}
	
	@WebResult(name = "list")
	public java.util.List<java.lang.Object> addAll(@WebParam(name = "list") java.util.List<java.lang.Object> list, @WebParam(name = "index") Integer index, @WebParam(name = "objects") java.util.List<java.lang.Object> objects) {
		if (list == null) {
			list = new ArrayList<java.lang.Object>();
		}
		if (index == null) {
			list.addAll(objects);
		}
		else {
			list.addAll(index, objects);
		}
		return list;
	}
	
	@WebResult(name = "list")
	public java.util.List<java.lang.Object> remove(@WebParam(name = "list") java.util.List<java.lang.Object> list, @WebParam(name = "object") java.lang.Object object) {
		if (list == null) {
			list = new ArrayList<java.lang.Object>();
		}
		list.remove(object);
		return list;
	}
	
	@WebResult(name = "list")
	public java.util.List<java.lang.Object> removeAll(@WebParam(name = "list") java.util.List<java.lang.Object> list, @WebParam(name = "objects") java.util.List<java.lang.Object> objects) {
		if (list == null) {
			list = new ArrayList<java.lang.Object>();
		}
		list.removeAll(objects);
		return list;
	}
	
	@WebResult(name = "list")
	public java.util.List<java.lang.Object> removeIndex(@WebParam(name = "list") java.util.List<java.lang.Object> list, @NotNull @WebParam(name = "index") Integer index) {
		if (list == null) {
			list = new ArrayList<java.lang.Object>();
		}
		list.remove((int) index);
		return list;
	}
	
	@WebResult(name = "list")
	public java.util.List<java.lang.Object> reverse(@WebParam(name = "list") java.util.List<java.lang.Object> list) {
		if (list == null) {
			list = new ArrayList<java.lang.Object>();
		}
		Collections.reverse(list);
		return list;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@WebResult(name = "list")
	public java.util.List<java.lang.Object> sort(@WebParam(name = "list") java.util.List<java.lang.Object> list, @WebParam(name = "comparatorService") java.lang.String comparatorServiceId) {
		if (list == null) {
			list = new ArrayList<java.lang.Object>();
		}
		if (comparatorServiceId != null) {
			DefinedService resolved = executionContext.getServiceContext().getResolver(DefinedService.class).resolve(comparatorServiceId);
			if (resolved == null) {
				throw new IllegalArgumentException("Invalid comparator service passed along: " + comparatorServiceId);
			}
			Comparator comparator = POJOUtils.newProxy(Comparator.class, resolved, ServiceRuntime.getRuntime().getRuntimeTracker(), executionContext);
			Collections.sort(list, comparator);
		}
		// we assume the list contains comparable items
		else {
			// circumventing generics...
			java.util.List tmp = list;
			Collections.sort((java.util.List<java.lang.Comparable>) tmp);
		}
		return list;
	}
	
	@WebResult(name = "list")
	public java.util.List<java.lang.Object> newList(@WebParam(name = "initialCapacity") Integer initialCapacity) {
		return initialCapacity == null ? new ArrayList<java.lang.Object>() : new ArrayList<java.lang.Object>(initialCapacity);
	}
}
