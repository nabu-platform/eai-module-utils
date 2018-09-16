package nabu.utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.eai.module.utils.CommonUtils;
import be.nabu.libs.converter.ConverterFactory;
import be.nabu.libs.evaluator.EvaluationException;
import be.nabu.libs.evaluator.PathAnalyzer;
import be.nabu.libs.evaluator.QueryParser;
import be.nabu.libs.evaluator.types.api.TypeOperation;
import be.nabu.libs.evaluator.types.operations.TypesOperationProvider;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.pojo.POJOUtils;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.structure.StructureInstanceDowncastReference;
import be.nabu.libs.types.structure.StructureInstanceUpcastReference;

@WebService
public class List {
	
	private ExecutionContext executionContext;
	
	public java.util.List<java.lang.Object> group(@WebParam(name = "list") java.util.List<java.lang.Object> instances, @NotNull @WebParam(name = "definition") java.lang.String name) {
		// detect the fields at each level and group the current list by them, do type masking on the inner document
		DefinedType resolve = executionContext.getServiceContext().getResolver(DefinedType.class).resolve(name);
		if (!(resolve instanceof ComplexType)) {
			throw new IllegalArgumentException("Can not find complex type: " + name);
		}
		return CommonUtils.group(instances, (ComplexType) resolve);
	}
	
	@SuppressWarnings("unchecked")
	@WebResult(name = "hashed")
	public java.util.Map<java.lang.String, java.util.List<java.lang.Object>> hash(@WebParam(name = "objects") java.util.List<java.lang.Object> documents, @NotNull @WebParam(name = "queries") java.util.List<java.lang.String> keys) throws ParseException, EvaluationException {
		java.util.Map<java.lang.String, java.util.List<java.lang.Object>> results = new HashMap<java.lang.String, java.util.List<java.lang.Object>>();
		if (documents != null) {
			java.util.List<TypeOperation> operations = new ArrayList<TypeOperation>();
			for (java.lang.String key : keys) {
				operations.add((TypeOperation) new PathAnalyzer<ComplexContent>(new TypesOperationProvider()).analyze(QueryParser.getInstance().parse(key)));
			}
			for (java.lang.Object document : documents) {
				if (document != null) {
					ComplexContent content = document instanceof ComplexContent 
						? (ComplexContent) document
						: ComplexContentWrapperFactory.getInstance().getWrapper().wrap(document);
						
					java.lang.String hash = null;
					for (TypeOperation operation : operations) {
						java.lang.Object evaluated = operation.evaluate(content);
						// properly convert
						if (evaluated != null && !(evaluated instanceof java.lang.String)) {
							java.lang.String stringified = ConverterFactory.getInstance().getConverter().convert(evaluated, java.lang.String.class);
							evaluated = stringified == null ? evaluated.toString() : stringified;
						}
						if (hash == null) {
							hash = "";
						}
						else {
							hash += ".";
						}
						hash += evaluated;
					}
					if (!results.containsKey(hash)) {
						results.put(hash, new ArrayList<java.lang.Object>());
					}
					results.get(hash).add(document);
				}
			}
		}
		return results;
	}
	
	@WebResult(name = "contains")
	public boolean contains(@WebParam(name = "list") java.util.List<java.lang.Object> list, @WebParam(name = "object") java.lang.Object object) {
		return list == null || indexOf(list, object) == null ? false : true;
	}
	
	@WebResult(name = "index")
	public Integer indexOf(@WebParam(name = "list") java.util.List<java.lang.Object> list, @WebParam(name = "object") java.lang.Object object) {
		if (list == null) {
			return null;
		}
		int index = list.indexOf(object);
		// because we do a lot of object wrapping for casting, check if the object is somewhere in the stack but hidden
		if (index < 0 && object instanceof ComplexContent) {
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i) instanceof ComplexContent) {
					if (unwrap(list.get(i)).contains(object)) {
						index = i;
						break;
					}
				}
			}
		}
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
	
	@WebResult(name = "element")
	public java.lang.Object get(@WebParam(name = "list") java.util.List<java.lang.Object> list, @NotNull @WebParam(name = "index") Integer index) {
		if (list == null) {
			list = new ArrayList<java.lang.Object>();
		}
		return index == null || index >= list.size() ? null : list.get(index);
	}
	
	@WebResult(name = "list")
	public java.util.List<java.lang.Object> getAll(@WebParam(name = "list") java.util.List<java.lang.Object> list, @WebParam(name = "fromInclusive") Integer from, @WebParam(name = "toExclusive") Integer to) {
		if (list == null) {
			list = new ArrayList<java.lang.Object>();
		}
		return list.subList(from == null ? 0 : from, to == null ? list.size() : java.lang.Math.min(list.size(), to));
	}

	@WebResult(name = "list")
	public java.util.List<java.lang.Object> set(@WebParam(name = "list") java.util.List<java.lang.Object> list, @NotNull @WebParam(name = "index") Integer index, @WebParam(name = "object") java.lang.Object object) {
		if (list == null) {
			list = new ArrayList<java.lang.Object>();
		}
		list.set(index, object);
		return list;
	}
	
	// unwraps the object into multiple objects that have been cast
	private java.util.List<java.lang.Object> unwrap(java.lang.Object object) {
		java.util.List<java.lang.Object> unwrapped = new ArrayList<java.lang.Object>();
		while (object != null) {
			unwrapped.add(object);
			if (object instanceof StructureInstanceDowncastReference) {
				object = ((StructureInstanceDowncastReference) object).getReference();
			}
			else if (object instanceof StructureInstanceUpcastReference) {
				object = ((StructureInstanceUpcastReference) object).getReference();
			}
			else {
				break;
			}
		}
		return unwrapped;
	}
	
	@WebResult(name = "list")
	public java.util.List<java.lang.Object> addAll(@WebParam(name = "list") java.util.List<java.lang.Object> list, @WebParam(name = "index") Integer index, @WebParam(name = "objects") java.util.List<java.lang.Object> objects) {
		if (list == null) {
			list = new ArrayList<java.lang.Object>();
		}
		if (objects != null) {
			if (index == null) {
				list.addAll(objects);
			}
			else {
				list.addAll(index, objects);
			}
		}
		return list;
	}
	
	@WebResult(name = "list")
	public java.util.List<java.lang.Object> remove(@WebParam(name = "list") java.util.List<java.lang.Object> list, @WebParam(name = "object") java.lang.Object object) {
		if (list == null) {
			list = new ArrayList<java.lang.Object>();
		}
		boolean removed = list.remove(object);
		if (!removed && object instanceof ComplexContent) {
			Integer index = indexOf(list, object);
			if (index != null) {
				list.remove((int) index);
			}
		}
		return list;
	}
	
	@WebResult(name = "list")
	public java.util.List<java.lang.Object> removeAll(@WebParam(name = "list") java.util.List<java.lang.Object> list, @WebParam(name = "objects") java.util.List<java.lang.Object> objects) {
		if (list == null) {
			list = new ArrayList<java.lang.Object>();
		}
		for (java.lang.Object object : objects) {
			remove(list, object);
		}
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
	public java.util.List<java.lang.Object> sort(@WebParam(name = "list") java.util.List<java.lang.Object> list, @WebParam(name = "comparatorService") java.lang.String comparatorServiceId, @WebParam(name = "fields") final java.util.List<java.lang.String> fields) {
		if (list == null) {
			list = new ArrayList<java.lang.Object>();
		}
		else {
			list = new ArrayList<java.lang.Object>(list);
		}
		if (comparatorServiceId != null) {
			DefinedService resolved = executionContext.getServiceContext().getResolver(DefinedService.class).resolve(comparatorServiceId);
			if (resolved == null) {
				throw new IllegalArgumentException("Invalid comparator service passed along: " + comparatorServiceId);
			}
			Comparator comparator = POJOUtils.newProxy(Comparator.class, resolved, executionContext);
			Collections.sort(list, comparator);
		}
		else if (fields != null && !fields.isEmpty()) {
			Comparator comparator = new Comparator() {
				@Override
				public int compare(java.lang.Object o1, java.lang.Object o2) {
					if (o1 == null) {
						if (o2 == null) {
							return 0;
						}
						else {
							return -1;
						}
					}
					else if (o2 == null) {
						return 1;
					}
					if (!(o1 instanceof ComplexContent)) {
						o1 = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(o1);
					}
					if (!(o2 instanceof ComplexContent)) {
						o2 = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(o2);
					}
					int comparison = 0;
					for (java.lang.String field : fields) {
						// fields should never have whitespace in them, but you can say "asc" or "desc" 
						java.lang.String [] parts = field.split("[\\s]+");
						java.lang.Object value1 = ((ComplexContent) o1).get(parts[0]);
						java.lang.Object value2 = ((ComplexContent) o2).get(parts[0]);
						if (value1 == null) {
							if (value2 == null) {
								continue;
							}
							else {
								comparison = -1;
								break;
							}
						}
						else if (value2 == null) {
							comparison = 1;
							break;
						}
						if (!(value1 instanceof Comparable) || !(value2 instanceof Comparable)) {
							throw new IllegalArgumentException("The fields can not be compared");
						}
						comparison = ((Comparable) value1).compareTo(value2);
						if (comparison != 0) {
							// if we asked for descending, reverse the comparison
							if (parts.length == 2 && parts[1].equalsIgnoreCase("desc")) {
								comparison *= -1;
							}
							break;
						}
					}
					return comparison;
				}
			};
			Collections.sort(list, comparator);
		}
		// we assume the list contains comparable items
		else {
			// circumventing generics...
			java.util.List tmp = list;
			Collections.sort(tmp, new Comparator<java.lang.Object>() {
				@Override
				public int compare(java.lang.Object o1, java.lang.Object o2) {
					return ((java.lang.Comparable) o1).compareTo((java.lang.Comparable) o2);
				}
			});
		}
		return list;
	}
	
	@WebResult(name = "list")
	public java.util.List<java.lang.Object> unique(@WebParam(name = "list") java.util.List<java.lang.Object> list) {
		if (list == null) {
			return null;
		}
		return new ArrayList<java.lang.Object>(new LinkedHashSet<java.lang.Object>(list));
	}
	
	@WebResult(name = "minimum")
	public java.lang.Object minimum(@WebParam(name = "list") java.util.List<java.lang.Object> list, @WebParam(name = "comparatorService") java.lang.String comparatorServiceId, @WebParam(name = "fields") java.util.List<java.lang.String> fields) {
		java.util.List<java.lang.Object> sort = sort(list, comparatorServiceId, fields);
		return sort.isEmpty() ? null : sort.get(0);
	}
	
	@WebResult(name = "maximum")
	public java.lang.Object maximum(@WebParam(name = "list") java.util.List<java.lang.Object> list, @WebParam(name = "comparatorService") java.lang.String comparatorServiceId, @WebParam(name = "fields") java.util.List<java.lang.String> fields) {
		java.util.List<java.lang.Object> sort = sort(list, comparatorServiceId, fields);
		return sort.isEmpty() ? null : sort.get(sort.size() - 1);
	}
	
	@WebResult(name = "list")
	public java.util.List<java.lang.Object> newList(@WebParam(name = "initialCapacity") Integer initialCapacity) {
		return initialCapacity == null ? new ArrayList<java.lang.Object>() : new ArrayList<java.lang.Object>(initialCapacity);
	}
	
	@WebResult(name = "linkedList")
	public java.util.List<java.lang.Object> newLinkedList() {
		return new LinkedList<java.lang.Object>();
	}
	
	@WebResult(name = "size")
	@NotNull
	public Integer size(@WebParam(name = "list") java.util.List<java.lang.Object> list) {
		return list == null ? 0 : list.size();
	}
}
