package nabu.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.lang.String;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService
public class Map {
	
	@WebResult(name = "properties")
	public List<Property> asProperties(@WebParam(name = "map") java.util.Map<String, String> map) {
		List<Property> properties = new ArrayList<Property>();
		for (String key : map.keySet()) {
			Property property = new Property();
			property.setKey(key);
			property.setValue(map.get(key));
			properties.add(property);
		}
		return properties;
	}
	
	@WebResult(name = "map")
	public java.util.Map<String, Object> create(@WebParam(name = "respectOrder") boolean respectOrder) {
		return respectOrder ? new LinkedHashMap<String, Object>() : new HashMap<String, Object>();
	}
	
	@WebResult(name = "keys")
	public List<String> keys(@WebParam(name = "map") java.util.Map<String, Object> map) {
		return new ArrayList<String>(map.keySet());
	}
	
	@WebResult(name = "value")
	public Object value(@WebParam(name = "map") java.util.Map<String, Object> map, @WebParam(name = "key") String key) {
		return map.get(key);
	}
	
	@WebResult(name = "values")
	public Collection<Object> values(@WebParam(name = "map") java.util.Map<String, Object> map) {
		return map.values();
	}
}
