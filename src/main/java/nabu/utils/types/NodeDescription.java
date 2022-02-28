package nabu.utils.types;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "nodeDescription")
@XmlType(propOrder = { "id", "type", "name", "artifactClass", "leaf", "nodes", "summary", "description", "tags", "comment" })
public class NodeDescription {

	private String type, name, artifactClass, id;
	private boolean leaf;
	private List<NodeDescription> nodes;
	private int priority;
	private String summary, description, comment;
	private List<String> tags;

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

	public List<NodeDescription> getNodes() {
		return nodes;
	}
	public void setNodes(List<NodeDescription> nodes) {
		this.nodes = nodes;
	}

	@XmlTransient
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
}
