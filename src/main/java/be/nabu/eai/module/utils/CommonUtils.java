package be.nabu.eai.module.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import be.nabu.libs.evaluator.impl.ClassicOperation;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.mask.MaskedContent;

public class CommonUtils {
	
	@SuppressWarnings("unchecked")
	public static List<Object> group(List<Object> instances, Integer depth, ComplexType type) {
		if (instances == null) {
			return null;
		}
		List<String> fieldNames = new ArrayList<String>();
		String groupName = null;
		ComplexType groupType = null;
		// detect the fields at each level and group the current list by them, do type masking on the inner document
		for (Element<?> child : TypeUtils.getAllChildren(type)) {
			if (child.getType().isList(child.getProperties())) {
				if (child.getType() instanceof ComplexType) {
					groupName = child.getName();
					groupType = (ComplexType) child.getType();
				}
			}
			else {
				if (child.getType() instanceof SimpleType) {
					fieldNames.add(child.getName());
				}
			}
		}
		// there is nothing to group
		if (groupType == null) {
			return null;
		}
		// if we have a group target, we need keys to group them by
		else if (fieldNames.isEmpty()) {
			throw new IllegalArgumentException("No grouping fields found for type: " + type);
		}
		// first we group our elements by the fieldNames
		Map<List<Object>, List<Object>> mapped = new LinkedHashMap<List<Object>, List<Object>>(); 
		for (Object instance : instances) {
			if (!(instance instanceof ComplexContent)) {
				instance = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(instance);
			}
			ComplexContent content = (ComplexContent) instance;
			List<Object> values = new ArrayList<Object>();
			for (String fieldName : fieldNames) {
				Object value = ClassicOperation.normalize(content.get(fieldName));
				values.add(value);
			}
			if (!mapped.containsKey(values)) {
				mapped.put(values, new ArrayList<Object>());
			}
			mapped.get(values).add(content);
		}
		// this boolean allows us to check only once if we need further grouping instead of for each value
		boolean grouping = depth == null || depth > 0;
		List<Object> result = new ArrayList<Object>();
		// set the field values
		for (List<Object> values : mapped.keySet()) {
			ComplexContent newInstance = type.newInstance();
			for (int i = 0; i < fieldNames.size(); i++) {
				newInstance.set(fieldNames.get(i), values.get(i));
			}
			if (grouping) {
				List<Object> group = group(mapped.get(values), depth == null ? null : depth - 1, groupType);
				// no further grouping necessary
				if (group == null) {
					grouping = false;
				}
				else {
					newInstance.set(groupName, group);
				}
			}
			// if we are no longer grouping, we need to set a masked list of items
			if (!grouping) {
				List<Object> masked = new ArrayList<Object>();
				for (Object single : mapped.get(values)) {
					masked.add(new MaskedContent((ComplexContent) single, groupType));
				}
				newInstance.set(groupName, masked);
			}
			result.add(newInstance);
		}
		return result;
	}
}
