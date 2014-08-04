package org.aimas.ami.cmm.agent.coordinator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topbraid.spin.inference.SPINInferences;
import org.topbraid.spin.util.CommandWrapper;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class CommandRule {
	protected Resource anchorResource;
	protected CommandWrapper commandWrapper;
	protected Map<String, RDFNode> bindings;
	protected Property commandProperty;

	protected Set<Resource> referencedAssertions;
		
	public CommandRule(Property commandProperty, Resource anchorResource, CommandWrapper commandWrapper, 
			Map<String, RDFNode> bindings) {
		
	    this.commandProperty = commandProperty;
		this.anchorResource = anchorResource;
	    this.commandWrapper = commandWrapper;
	    this.bindings = bindings;
    }

	public List<CommandResult> execute(Model queryModel) {
		Model newTriples = ModelFactory.createDefaultModel();
		
		Map<Resource, List<CommandWrapper>> cls2Query = new HashMap<Resource, List<CommandWrapper>>();
		Map<Resource, List<CommandWrapper>> cls2Constructor = new HashMap<Resource, List<CommandWrapper>>();
		Map<CommandWrapper, Map<String, RDFNode>> templateBindings = new HashMap<CommandWrapper, Map<String, RDFNode>>();
		
		templateBindings.put(commandWrapper, bindings);
		List<CommandWrapper> entityCommandWrappers = new LinkedList<CommandWrapper>();
		entityCommandWrappers.add(commandWrapper);
		cls2Query.put(anchorResource, entityCommandWrappers);
		
		SPINInferences.run(queryModel, newTriples, cls2Query, cls2Constructor, templateBindings, 
				null, null, true, commandProperty, null, null);
		
		if (!newTriples.isEmpty()) {
			return CommandResult.createResult(newTriples);
		}
		
		return null;
	}
}
