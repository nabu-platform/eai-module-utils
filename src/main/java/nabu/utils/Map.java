/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
import be.nabu.libs.types.map.MapContentWrapper;
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
	
	@WebResult(name = "object")
	public java.lang.Object toObject(@WebParam(name = "map") java.util.Map<String, Object> map) {
		return new MapContentWrapper().wrap(map);
	}
}
