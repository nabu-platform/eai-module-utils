package nabu.utils.types;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "nodeDescription")
public class NodeDescription {

	private String type, name, artifactClass, id;
	private boolean leaf;

	public NodeDescription() {
		// auto construct
	}
	
	public NodeDescription(String id, String name, String type, String artifactClass, boolean leaf) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.artifactClass = artifactClass;
		this.leaf = leaf;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getArtifactClass() {
		return artifactClass;
	}
	public void setArtifactClass(String artifactClass) {
		this.artifactClass = artifactClass;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public boolean isLeaf() {
		return leaf;
	}
	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

}
