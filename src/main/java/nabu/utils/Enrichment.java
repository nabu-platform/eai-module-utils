package nabu.utils;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.libs.property.ValueUtils;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.ServiceException;
import be.nabu.libs.types.DefinedTypeResolverFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.KeyValuePair;
import be.nabu.libs.types.properties.EnricherProperty;
import be.nabu.libs.types.properties.PersisterProperty;
import nabu.utils.types.EnrichmentConfiguration;
import nabu.utils.types.Property;

@WebService
public class Enrichment {
	
	private ExecutionContext executionContext;
	
	public void apply(@WebParam(name = "objects") List<java.lang.Object> objects, @WebParam(name = "language") java.lang.String language) throws ServiceException {
		EAIRepositoryUtils.enrich(objects, language, executionContext);
	}
	
	public void persist(@WebParam(name = "objects") List<java.lang.Object> objects) {
		// TODO
	}
	
	@WebResult(name = "configuration")
	public EnrichmentConfiguration configuration(@WebParam(name = "typeId") java.lang.String typeId, @WebParam(name = "field") java.lang.String fieldName) {
		DefinedType resolved = DefinedTypeResolverFactory.getInstance().getResolver().resolve(typeId);
		Element<?> field = ((ComplexType) resolved).get(fieldName);
		EnrichmentConfiguration configuration = new EnrichmentConfiguration();
		
		List<Value<?>> allProperties = TypeUtils.getAllProperties(field);
		List<KeyValuePair> properties = new ArrayList<KeyValuePair>();
		java.lang.String stringProperties = ValueUtils.getValue(EnricherProperty.getInstance(), allProperties);
		if (stringProperties != null) {
			for (java.lang.String part : stringProperties.replaceAll("^[^;]+", "").split(";")) {
				if (!part.trim().isEmpty()) {
					java.lang.String[] split = part.split("=");
					properties.add(new Property(split[0], split.length >= 2 ? split[1] : null));
				}
			}
		}
		configuration.setEnrich(properties);
		
		properties = new ArrayList<KeyValuePair>();
		stringProperties = ValueUtils.getValue(PersisterProperty.getInstance(), allProperties);
		if (stringProperties != null) {
			for (java.lang.String part : stringProperties.replaceAll("^[^;]+", "").split(";")) {
				if (!part.trim().isEmpty()) {
					java.lang.String[] split = part.split("=");
					properties.add(new Property(split[0], split.length >= 2 ? split[1] : null));
				}
			}
		}
		configuration.setPersist(properties);
		
		return configuration;
	}

}
