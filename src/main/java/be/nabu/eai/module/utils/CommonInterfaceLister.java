package be.nabu.eai.module.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import be.nabu.eai.developer.api.InterfaceLister;
import be.nabu.eai.developer.util.InterfaceDescriptionImpl;

public class CommonInterfaceLister implements InterfaceLister {
	
	private static Collection<InterfaceDescription> descriptions = null;
	
	@Override
	public Collection<InterfaceDescription> getInterfaces() {
		if (descriptions == null) {
			synchronized(CommonInterfaceLister.class) {
				if (descriptions == null) {
					List<InterfaceDescription> descriptions = new ArrayList<InterfaceDescription>();
					descriptions.add(new InterfaceDescriptionImpl("Common", "Comparator", "java.util.Comparator.compare"));
					descriptions.add(new InterfaceDescriptionImpl("Cache", "Annotater", "be.nabu.libs.cache.api.CacheAnnotater.annotate"));
					descriptions.add(new InterfaceDescriptionImpl("Features", "Lister", "be.nabu.eai.repository.api.FeatureProviderService.features"));
					CommonInterfaceLister.descriptions = descriptions;
				}
			}
		}
		return descriptions;
	}

}
