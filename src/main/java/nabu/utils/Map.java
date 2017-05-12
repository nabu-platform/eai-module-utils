	package nabu.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import java.lang.String;
import java.lang.Object;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.libs.types.api.KeyValuePair;
import nabu.utils.types.Property;

@WebService
public class Map {
	
	public enum MapOrder {
		NONE,
		INSERTION,
		NATURAL
	}
	
	@WebResult(name = "properties")
	public List<KeyValuePair> toProperties(@WebParam(name = "map") java.util.Map<String, String> map) {
		List<KeyValuePair> properties = new ArrayList<KeyValuePair>();
		if (map != null) {
			for (String key : map.keySet()) {
				Property property = new Property();
				property.setKey(key);
				property.setValue(map.get(key));
				properties.add(property);
			}
		}
		return properties;
	}
	
	@WebResult(name = "previousValue")
	public Object put(@WebParam(name = "map") java.util.Map<String, Object> map, @WebParam(name = "key") String key, @WebParam(name = "value") Object value) {
		return map == null ? null : map.put(key, value);
	}
	
	@WebResult(name = "map")
	public java.util.Map<String, Object> create(@WebParam(name = "order") MapOrder order) {
		if (order == null || order == MapOrder.NONE) {
			return new HashMap<String, Object>();
		}
		else if (order == MapOrder.INSERTION) {
			return new LinkedHashMap<String, Object>();
		}
		return new TreeMap<String, Object>();
	}
	
	@WebResult(name = "keys")
	public List<String> keys(@WebParam(name = "map") java.util.Map<String, Object> map) {
		return map == null ? new ArrayList<String>() : new ArrayList<String>(map.keySet());
	}
	
	@WebResult(name = "value")
	public Object get(@WebParam(name = "map") java.util.Map<String, Object> map, @WebParam(name = "key") String key) {
		return map == null ? null : map.get(key);
	}
	
	@WebResult(name = "values")
	public Collection<Object> values(@WebParam(name = "map") java.util.Map<String, Object> map) {
		return map == null ? new ArrayList<Object>() : map.values();
	}
}
