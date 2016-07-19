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
