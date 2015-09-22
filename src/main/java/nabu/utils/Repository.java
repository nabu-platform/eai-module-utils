package nabu.utils;

import java.lang.String;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebResult;
import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Node;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;

@WebService
public class Repository {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@WebResult(name = "complexTypes")
	public List<ComplexType> getComplexTypes() {
		List<ComplexType> artifacts = new ArrayList<ComplexType>();
		EAIResourceRepository instance = EAIResourceRepository.getInstance();
		if (instance != null) {
			for (Node node : instance.getNodes(DefinedType.class)) {
				try {
					if (node.getArtifact() instanceof ComplexType) {
						artifacts.add((ComplexType) node.getArtifact());
					}
				}
				catch (Exception e) {
					logger.error("Could not load: " + node, e);
				}
			}
		}
		return artifacts;
	}
	
	@WebResult(name = "complexType")
	public ComplexType getComplexType(String id) throws IOException, ParseException {
		EAIResourceRepository instance = EAIResourceRepository.getInstance();
		Node node = instance.getNode(id);
		if (node != null && node.getArtifact() instanceof ComplexType) {
			return (ComplexType) node.getArtifact();
		}
		return null;
	}
}
