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
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "nodeDescription")
@XmlType(propOrder = { "inputs", "outputs", "inputName", "outputName" })
public class ServiceDescription extends NodeDescription {
	private List<ParameterDescription> inputs, outputs;
	private String inputName, outputName;

	public List<ParameterDescription> getInputs() {
		return inputs;
	}
	public void setInputs(List<ParameterDescription> inputs) {
		this.inputs = inputs;
	}

	public List<ParameterDescription> getOutputs() {
		return outputs;
	}
	public void setOutputs(List<ParameterDescription> outputs) {
		this.outputs = outputs;
	}
	public String getInputName() {
		return inputName;
	}
	public void setInputName(String inputName) {
		this.inputName = inputName;
	}
	public String getOutputName() {
		return outputName;
	}
	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}
}
