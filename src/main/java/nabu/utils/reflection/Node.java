package nabu.utils.reflection;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Entry;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.SimpleType;
import nabu.utils.types.NodeDescription;

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
