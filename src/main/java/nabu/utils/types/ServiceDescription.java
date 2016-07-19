package nabu.utils.types;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "nodeDescription")
@XmlType(propOrder = { "inputs", "outputs" })
public class ServiceDescription extends NodeDescription {
	private List<ParameterDescription> inputs, outputs;

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
	
}
