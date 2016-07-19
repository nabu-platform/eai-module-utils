package nabu.utils.reflection;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Entry;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.properties.CommentProperty;
import be.nabu.libs.types.properties.MaxOccursProperty;
import nabu.utils.types.NodeDescription;
import nabu.utils.types.ParameterDescription;
import nabu.utils.types.ServiceDescription;

@WebService
public class Node {
	
	@WebResult(name = "nodes")
	public List<NodeDescription> list(@WebParam(name = "id") String id, @WebParam(name = "recursive") Boolean recursive) {
		Entry entry = id == null 
			? EAIResourceRepository.getInstance().getRoot()
			: EAIResourceRepository.getInstance().getEntry(id);
		List<NodeDescription> nodes = new ArrayList<NodeDescription>();
		if (entry != null) {
			for (Entry child : entry) {
				NodeDescription description = getDescription(child, recursive);
				nodes.add(description); 
			}
		}
		return nodes;
	}
	
	@WebResult(name = "services")
	public List<ServiceDescription> services(@WebParam(name = "id") String id, @WebParam(name = "recursive") Boolean recursive) {
		Entry entry = id == null 
			? EAIResourceRepository.getInstance().getRoot()
			: EAIResourceRepository.getInstance().getEntry(id);
		List<ServiceDescription> nodes = new ArrayList<ServiceDescription>();
		if (entry != null) {
			getAllServices(entry, nodes, recursive);
		}
		return nodes;
	}

	private void getAllServices(Entry entry, List<ServiceDescription> nodes, Boolean recursive) {
		getSingleDescription(nodes, entry);
		for (Entry child : entry) {
			getSingleDescription(nodes, child);
			if (recursive != null && recursive) {
				getAllServices(child, nodes, recursive);
			}
		}
	}

	private void getSingleDescription(List<ServiceDescription> nodes, Entry child) {
		if (child.isNode() && Service.class.isAssignableFrom(child.getNode().getArtifactClass())) {
			try {
				ServiceDescription description = getServiceDescription(child);
				nodes.add(description); 
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private ServiceDescription getServiceDescription(Entry child) throws IOException, ParseException {
		ServiceDescription description = new ServiceDescription();
		description.setId(child.getId());
		description.setName(child.getName());
		description.setType("service");
		description.setArtifactClass(child.getNode().getArtifactClass().getName());
		description.setLeaf(child.isLeaf());
		DefinedService service = (DefinedService) child.getNode().getArtifact();
		description.setInputName(service.getServiceInterface().getInputDefinition().getName());
		description.setOutputName(service.getServiceInterface().getOutputDefinition().getName());
		description.setInputs(toParameters(service.getServiceInterface().getInputDefinition()));
		description.setOutputs(toParameters(service.getServiceInterface().getOutputDefinition()));
		return description;
	}
	
	private static List<ParameterDescription> toParameters(ComplexType type) {
		List<ParameterDescription> parameters = new ArrayList<ParameterDescription>();
		for (Element<?> element : TypeUtils.getAllChildren(type)) {
			Value<Integer> maxOccurs = element.getProperty(MaxOccursProperty.getInstance());
			Value<String> comment = element.getProperty(CommentProperty.getInstance());
			parameters.add(new ParameterDescription(element.getName(), element.getType() instanceof DefinedType ? ((DefinedType) element.getType()).getId() : null, 
				comment == null ? null : comment.getValue(),
				maxOccurs != null && maxOccurs.getValue() != null && maxOccurs.getValue() != 1));
		}
		return parameters;
	}

	private NodeDescription getDescription(Entry entry, Boolean recursive) {
		String type = null;
		String artifactClass = null;
		if (entry.isNode()) {
			artifactClass = entry.getNode().getArtifactClass().getName();
			if (Service.class.isAssignableFrom(entry.getNode().getArtifactClass())) {
				type = "service";
			}
			else if (ComplexType.class.isAssignableFrom(entry.getNode().getArtifactClass())) {
				type = "complexType";
			}
			else if (SimpleType.class.isAssignableFrom(entry.getNode().getArtifactClass())) {
				type = "simpleType";
			}
		}
		NodeDescription description = new NodeDescription(entry.getId(), entry.getName(), type, artifactClass, entry.isLeaf());
		if (recursive != null && recursive) {
			List<NodeDescription> children = new ArrayList<NodeDescription>();
			for (Entry child : entry) {
				children.add(getDescription(child, recursive));
			}
			description.setNodes(children);
		}
		return description;
	}
	
}
