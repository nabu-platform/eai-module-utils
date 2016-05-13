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
	public List<NodeDescription> list(@WebParam(name = "id") String id) {
		Entry entry = id == null 
			? EAIResourceRepository.getInstance().getRoot()
			: EAIResourceRepository.getInstance().getEntry(id);
		List<NodeDescription> nodes = new ArrayList<NodeDescription>();
		if (entry != null) {
			for (Entry child : entry) {
				String type = null;
				String artifactClass = null;
				if (child.isNode()) {
					artifactClass = child.getNode().getArtifactClass().getName();
					if (Service.class.isAssignableFrom(child.getNode().getArtifactClass())) {
						type = "service";
					}
					else if (ComplexType.class.isAssignableFrom(child.getNode().getArtifactClass())) {
						type = "complexType";
					}
					else if (SimpleType.class.isAssignableFrom(child.getNode().getArtifactClass())) {
						type = "simpleType";
					}
				}
				nodes.add(new NodeDescription(child.getId(), child.getName(), type, artifactClass, child.isLeaf())); 
			}
		}
		return nodes;
	}
	
}
