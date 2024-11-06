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
