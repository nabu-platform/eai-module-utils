package nabu.utils.types;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import be.nabu.libs.validator.api.Validation;

@XmlRootElement
public class StartableNode {
	private boolean started, canStop;
	private String id, type, artifactClass;
	private List<Validation<?>> messages;
	
	public boolean isStarted() {
		return started;
	}
	public void setStarted(boolean started) {
		this.started = started;
	}
	public boolean isCanStop() {
		return canStop;
	}
	public void setCanStop(boolean canStop) {
		this.canStop = canStop;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<Validation<?>> getMessages() {
		return messages;
	}
	public void setMessages(List<Validation<?>> messages) {
		this.messages = messages;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getArtifactClass() {
		return artifactClass;
	}
	public void setArtifactClass(String artifactClass) {
		this.artifactClass = artifactClass;
	}
}
