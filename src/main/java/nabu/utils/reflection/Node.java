package nabu.utils.reflection;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Entry;
import be.nabu.libs.artifacts.ArtifactUtils;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.artifacts.api.ArtifactWithExceptions;
import be.nabu.libs.artifacts.api.ExceptionDescription;
import be.nabu.libs.artifacts.api.ExternalDependency;
import be.nabu.libs.artifacts.api.ExternalDependencyArtifact;
import be.nabu.libs.artifacts.api.RestartableArtifact;
import be.nabu.libs.artifacts.api.StartableArtifact;
import be.nabu.libs.artifacts.api.StoppableArtifact;
import be.nabu.libs.artifacts.api.TwoPhaseStartableArtifact;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.base.Scope;
import be.nabu.libs.types.properties.CommentProperty;
import be.nabu.libs.types.properties.GeneratedProperty;
import be.nabu.libs.types.properties.IdentifiableProperty;
import be.nabu.libs.types.properties.MaxOccursProperty;
import be.nabu.libs.types.properties.MinOccursProperty;
import be.nabu.libs.types.properties.PatternProperty;
import be.nabu.libs.types.properties.ScopeProperty;
import be.nabu.libs.validator.api.Validation;
import nabu.utils.types.NodeDescription;
import nabu.utils.types.ParameterDescription;
import nabu.utils.types.ServiceDescription;
import nabu.utils.types.StartableNode;

@WebService
public class Node {
	
	private void getAllReferences(String id, Set<String> references, boolean recursive) {
		for (String child : EAIResourceRepository.getInstance().getReferences(id)) {
			if (child == null) {
				continue;
			}
			// java-based arrays like [B don't need to be added...
			if (!references.contains(child) && !child.startsWith("[")) {
				references.add(child);
				if (recursive) {
					getAllReferences(child, references, recursive);
				}
			}
		}
	}
	
	private void getAllDependencies(String id, Set<String> dependencies, boolean recursive) {
		for (String child : EAIResourceRepository.getInstance().getDependencies(id)) {
			if (!dependencies.contains(child)) {
				dependencies.add(child);
				if (recursive) {
					getAllDependencies(child, dependencies, recursive);
				}
			}
		}
	}
	
	@WebResult(name = "nodes")
	public List<NodeDescription> references(@NotNull @WebParam(name = "id") String id, @WebParam(name = "recursive") Boolean recursive) {
		Set<String> references = new LinkedHashSet<String>();
		references.add(id);
		getAllReferences(id, references, recursive != null && recursive);
		List<NodeDescription> nodes = new ArrayList<NodeDescription>();
		for (String child : references) {
			Entry entry = EAIResourceRepository.getInstance().getEntry(child);
			if (entry != null) {
				NodeDescription description = getDescription(entry, false);
				nodes.add(description); 
			}
		}
		return nodes;
	}
	
	@WebResult(name = "nodes")
	public List<NodeDescription> dependencies(@NotNull @WebParam(name = "id") String id, @WebParam(name = "recursive") Boolean recursive) {
		Set<String> dependencies = new LinkedHashSet<String>();
		dependencies.add(id);
		getAllDependencies(id, dependencies, recursive != null && recursive);
		List<NodeDescription> nodes = new ArrayList<NodeDescription>();
		for (String child : dependencies) {
			Entry entry = EAIResourceRepository.getInstance().getEntry(child);
			if (entry != null) {
				NodeDescription description = getDescription(entry, false);
				nodes.add(description); 
			}
		}
		return nodes;
	}
	
	@WebResult(name = "externalDependencies")
	public List<ExternalDependency> externalDependencies(@WebParam(name = "id") String id) {
		List<ExternalDependency> dependencies = new ArrayList<ExternalDependency>();
		Entry entry = EAIResourceRepository.getInstance().getEntry(id);
		if (entry.isNode()) {
			Set<String> references = new LinkedHashSet<String>();
			getAllReferences(id, references, true);
			for (String reference : references) {
				Artifact resolve = EAIResourceRepository.getInstance().resolve(reference);
				if (resolve instanceof ExternalDependencyArtifact) {
					dependencies.addAll(((ExternalDependencyArtifact) resolve).getExternalDependencies());
				}
			}
		}
		else {
			for (ExternalDependencyArtifact artifact : EAIResourceRepository.getInstance().getArtifacts(ExternalDependencyArtifact.class)) {
				if (id == null || artifact.getId().equals(id) || artifact.getId().startsWith(id + ".")) {
					dependencies.addAll(artifact.getExternalDependencies());
				}
			}
		}
		return dependencies;
	}
	
	@SuppressWarnings("unchecked")
	@WebResult(name = "resolved")
	public NodeDescription resolveFor(@WebParam(name = "id") String id, @NotNull @WebParam(name = "type") String artifactClass) throws ClassNotFoundException {
		if (id == null) {
			// we get the calling service (if any)
			ServiceRuntime parent = ServiceRuntime.getRuntime().getParent();
			if (parent == null || !(parent.getService() instanceof DefinedService)) {
				throw new IllegalStateException("If you don't explicitly fill in an id, you need to call this service from another (defined) service");
			}
			id = ((DefinedService) parent.getService()).getId();
		}
		Class<Artifact> loadClass = null;
		try {
			loadClass = (Class<Artifact>) Thread.currentThread().getContextClassLoader().loadClass(artifactClass);
		}
		catch (Exception e) {
			// this should not fail if for example the type does not exist, it should simply return nothing
		}
		Artifact resolveFor = EAIResourceRepository.getInstance().resolveFor(id, loadClass);
		return resolveFor == null ? null : getDescription(EAIResourceRepository.getInstance().getEntry(resolveFor.getId()), false);
	}
	
	@WebResult(name = "nodes")
	public List<NodeDescription> listByType(@WebParam(name = "id") String id, @NotNull @WebParam(name = "type") String artifactClass) throws ClassNotFoundException {
		List<NodeDescription> nodes = new ArrayList<NodeDescription>();
		Class<?> loadClass = null;
		try {
			loadClass = Thread.currentThread().getContextClassLoader().loadClass(artifactClass);
		}
		catch (Exception e) {
			// this should not fail if for example the type does not exist, it should simply return nothing
		}
		if (loadClass != null) {
			List<?> artifacts = EAIResourceRepository.getInstance().getArtifacts(loadClass);
			for (Object artifact : artifacts) {
				if (id == null || ((Artifact) artifact).getId().startsWith(id + ".")) {
					nodes.add(getDescription(EAIResourceRepository.getInstance().getEntry(((Artifact) artifact).getId()), false));
				}
			}
		}
		return nodes;
	}
	
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
	
	@WebResult(name = "node")
	public NodeDescription get(@WebParam(name = "id") String id) {
		Entry entry = id == null 
			? EAIResourceRepository.getInstance().getRoot()
			: EAIResourceRepository.getInstance().getEntry(id);
		return getDescription(entry, false);
	}
	
	@WebResult(name = "services")
	public List<ServiceDescription> services(@WebParam(name = "id") String id, @WebParam(name = "recursive") Boolean recursive) {
		Entry entry = id == null 
			? EAIResourceRepository.getInstance().getRoot()
			: EAIResourceRepository.getInstance().getEntry(id);
		List<ServiceDescription> nodes = new ArrayList<ServiceDescription>();
		if (entry != null) {
			getAllServices(entry, nodes, recursive);
		}
		return nodes;
	}

	private void getAllServices(Entry entry, List<ServiceDescription> nodes, Boolean recursive) {
		getSingleDescription(nodes, entry);
		for (Entry child : entry) {
			getSingleDescription(nodes, child);
			if (recursive != null && recursive) {
				getAllServices(child, nodes, recursive);
			}
		}
	}

	private void getSingleDescription(List<ServiceDescription> nodes, Entry child) {
		if (child.isNode() && Service.class.isAssignableFrom(child.getNode().getArtifactClass())) {
			try {
				ServiceDescription description = getServiceDescription(child);
				nodes.add(description); 
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private ServiceDescription getServiceDescription(Entry child) throws IOException, ParseException {
		ServiceDescription description = new ServiceDescription();
		description.setId(child.getId());
		description.setName(child.getName());
		description.setType("service");
		description.setArtifactClass(child.getNode().getArtifactClass().getName());
		description.setLeaf(child.isLeaf());
		DefinedService service = (DefinedService) child.getNode().getArtifact();
		if (service == null) {
			throw new RuntimeException("Could not get service from entry: " + child.getId());
		}
		else if (service.getServiceInterface() == null) {
			throw new RuntimeException("The service '" + service.getId() + "' has no interface");
		}
		description.setInputName(service.getServiceInterface().getInputDefinition().getName());
		description.setOutputName(service.getServiceInterface().getOutputDefinition().getName());
		description.setInputs(toParameters(service.getServiceInterface().getInputDefinition()));
		description.setOutputs(toParameters(service.getServiceInterface().getOutputDefinition()));
		return description;
	}
	
	public static List<ParameterDescription> toParameters(ComplexType type) {
		return toParameters(type, false);
	}
	
	public static List<ParameterDescription> toParameters(ComplexType type, boolean recursive) {
		return toParameters(type, recursive, null, new ArrayList<ComplexType>());
	}
	
	private static List<ParameterDescription> toParameters(ComplexType type, boolean recursive, String parent, List<ComplexType> ignore) {
		List<ParameterDescription> parameters = new ArrayList<ParameterDescription>();
		for (Element<?> element : TypeUtils.getAllChildren(type)) {
			Value<Integer> maxOccurs = element.getProperty(MaxOccursProperty.getInstance());
			Value<Integer> minOccurs = element.getProperty(MinOccursProperty.getInstance());
			Value<String> comment = element.getProperty(CommentProperty.getInstance());
			Value<Boolean> generatedProperty = element.getProperty(GeneratedProperty.getInstance());
			Value<Boolean> identifiableProperty = element.getProperty(IdentifiableProperty.getInstance());
			Value<String> pattern = element.getProperty(PatternProperty.getInstance());
			String childName = (parent == null ? "" : parent + ".") + element.getName();
			ParameterDescription description = new ParameterDescription(childName, element.getType() instanceof DefinedType ? ((DefinedType) element.getType()).getId() : null,
				element.getType().getName(element.getProperties()),
				comment == null ? null : comment.getValue(),
				maxOccurs != null && maxOccurs.getValue() != null && maxOccurs.getValue() != 1,
				minOccurs != null && minOccurs.getValue() != null && minOccurs.getValue() == 0,
				element.getType() instanceof SimpleType,
				generatedProperty != null && generatedProperty.getValue() != null && generatedProperty.getValue(),
				identifiableProperty != null && identifiableProperty.getValue() != null && identifiableProperty.getValue());
			Value<Scope> scope = element.getProperty(ScopeProperty.getInstance());
			description.setScope(scope != null ? scope.getValue() : null);
			description.setPattern(pattern == null ? null : pattern.getValue());
			// TODO: add more
			parameters.add(description);
			// TODO: in the future maybe you can choose whether you want the recursiveness to be done in a single long list or nested
			// the nested has much less use atm
			if (element.getType() instanceof ComplexType && recursive && !ignore.contains(element.getType())) {
				// we want the ignore to only apply to child resolves, not to sibling resolves
				List<ComplexType> childIgnore = new ArrayList<ComplexType>(ignore);
				childIgnore.add((ComplexType) element.getType());
				parameters.addAll(toParameters((ComplexType) element.getType(), recursive, childName, childIgnore));
			}
		}
		return parameters;
	}

	static NodeDescription getDescription(Entry entry, Boolean recursive) {
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
	
	@WebResult(name = "node")
	public StartableNode startable(@WebParam(name = "nodeId") String id) {
		Artifact artifact = EAIResourceRepository.getInstance().resolve(id);
		if (artifact instanceof StartableArtifact) {
			return createStartable(artifact);
		}
		return null;
	}

	private StartableNode createStartable(Artifact artifact) {
		StartableNode node = new StartableNode();
		node.setId(artifact.getId());
		node.setStarted(((StartableArtifact) artifact).isStarted());
		node.setCanStop(artifact instanceof StoppableArtifact);
		Map<String, List<Validation<?>>> messages = EAIResourceRepository.getInstance().getMessages(artifact.getId());
		if (messages != null) {
			node.setMessages(messages.get("start"));
		}
		if (artifact instanceof DefinedService) {
			node.setType(DefinedService.class.getName());
		}
		else if (artifact instanceof DefinedType) {
			node.setType(DefinedType.class.getName());
		}
		else {
			node.setType(artifact.getClass().getName());
		}
		node.setArtifactClass(artifact.getClass().getName());
		return node;
	}
	
	@WebResult(name = "nodes")
	public List<StartableNode> startables() {
		List<StartableNode> nodes = new ArrayList<StartableNode>();
		List<StartableArtifact> artifacts = EAIResourceRepository.getInstance().getArtifacts(StartableArtifact.class);
		for (StartableArtifact artifact : artifacts) {
			nodes.add(createStartable(artifact));
		}
		return nodes;
	}
	
	public void start(@WebParam(name = "nodeId") String nodeId) throws Exception {
		if (nodeId != null) {
			Artifact artifact = EAIResourceRepository.getInstance().resolve(nodeId);
			if (artifact instanceof StartableArtifact && !((StartableArtifact) artifact).isStarted()) {
				try {
					((StartableArtifact) artifact).start();
					if (artifact instanceof TwoPhaseStartableArtifact) {
						((TwoPhaseStartableArtifact) artifact).finish();
					}
					EAIRepositoryUtils.message(EAIResourceRepository.getInstance(), artifact.getId(), "start", true);
				}
				catch (Exception e) {
					EAIRepositoryUtils.message(EAIResourceRepository.getInstance(), artifact.getId(), "start", true, EAIRepositoryUtils.toValidation(e));
					throw e;
				}
			}
		}
	}
	
	public void restart(@WebParam(name = "nodeId") String nodeId) throws Exception {
		if (nodeId != null) {
			Artifact artifact = EAIResourceRepository.getInstance().resolve(nodeId);
			if (artifact instanceof RestartableArtifact) {
				((RestartableArtifact) artifact).restart();
			}
			if (artifact instanceof StartableArtifact && artifact instanceof StoppableArtifact) {
				try {
					if (((StartableArtifact) artifact).isStarted()) {
						((StoppableArtifact) artifact).stop();
					}
					((StartableArtifact) artifact).start();
					EAIRepositoryUtils.message(EAIResourceRepository.getInstance(), artifact.getId(), "start", true);
				}
				catch (Exception e) {
					EAIRepositoryUtils.message(EAIResourceRepository.getInstance(), artifact.getId(), "start", true, EAIRepositoryUtils.toValidation(e));
					throw e;
				}				
			}
		}
	}
	
	public void stop(@WebParam(name = "nodeId") String nodeId) throws Exception {
		if (nodeId != null) {
			Artifact artifact = EAIResourceRepository.getInstance().resolve(nodeId);
			if (artifact instanceof StoppableArtifact) {
				try {
					((StoppableArtifact) artifact).stop();
					EAIRepositoryUtils.message(EAIResourceRepository.getInstance(), artifact.getId(), "stop", true);
				}
				catch (Exception e) {
					EAIRepositoryUtils.message(EAIResourceRepository.getInstance(), artifact.getId(), "stop", true, EAIRepositoryUtils.toValidation(e));
					throw e;
				}
			}
		}
	}
	
	@WebResult(name = "exceptions")
	public List<ExceptionDescription> exceptions(@WebParam(name = "entryId") String entryId, @WebParam(name = "recursive") Boolean recursive) throws IOException, ParseException {
		List<ExceptionDescription> descriptions = new ArrayList<ExceptionDescription>();
		Entry entry = entryId == null ? EAIResourceRepository.getInstance().getRoot() : EAIResourceRepository.getInstance().getEntry(entryId);
		exceptions(entry, descriptions, recursive != null && recursive);
		return ArtifactUtils.unique(descriptions);
	}
	
	private void exceptions(Entry entry, List<ExceptionDescription> descriptions, boolean recursive) throws IOException, ParseException {
		if (entry.isNode() && entry.getNode().getArtifact() instanceof ArtifactWithExceptions) {
			List<ExceptionDescription> exceptions = ((ArtifactWithExceptions) entry.getNode().getArtifact()).getExceptions();
			if (exceptions != null) {
				descriptions.addAll(exceptions);
			}
		}
		if (recursive) {
			for (Entry child : entry) {
				exceptions(child, descriptions, recursive);
			}
		}
	}
}
