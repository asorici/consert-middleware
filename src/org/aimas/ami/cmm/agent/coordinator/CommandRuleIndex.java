package org.aimas.ami.cmm.agent.coordinator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aimas.ami.cmm.functions.FunctionIndex;
import org.aimas.ami.cmm.vocabulary.CoordConf;
import org.topbraid.spin.util.CommandWrapper;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.SPINQueryFinder;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class CommandRuleIndex {
	private CommandManager commandManager;
	
	private List<OntProperty> commandRuleProperties;
	private Map<OntProperty, List<CommandRule>> commandRuleMap; 
	
	public CommandRuleIndex(CommandManager commandManager, OntModel controlModel) {
	    this.commandManager = commandManager;
	    createIndex(controlModel);
    }
	
	public List<OntProperty> getCommandRuleProperties() {
		return commandRuleProperties;
	}
	
	public List<CommandRule> getCommandRules(OntProperty commandRuleProperty) {
		return commandRuleMap.get(commandRuleProperty);
	}
	
	private void createIndex(OntModel controlModel) {
	    // First, register all SPIN rule templates and functions defined in this controlModel
		FunctionIndex.registerCustomFunctions(controlModel);
		
		// Then, identify all subProperties of hasCommandRule and form an ordered index thereof
		OntProperty hasCommandRule = controlModel.getOntProperty(CoordConf.hasCommandRule.getURI());
		System.out.println(hasCommandRule);
		
		List<? extends OntProperty> ruleProperties = hasCommandRule.listSubProperties(true).toList();
		commandRuleProperties = orderCommmandRules(ruleProperties);
		
		// Next, identify the CommandRuleTemplates attached to each commandRule property
		commandRuleMap = new HashMap<OntProperty, List<CommandRule>>();
		Map<CommandWrapper, Map<String,RDFNode>> initialTemplateBindings = new HashMap<CommandWrapper, Map<String,RDFNode>>();
		
		for (OntProperty cmdProp : commandRuleProperties) {
			List<CommandRule> commandRules = new LinkedList<CommandRule>();
			
			Map<Resource,List<CommandWrapper>> cls2Query = SPINQueryFinder.getClass2QueryMap(
				controlModel, controlModel, cmdProp, false, initialTemplateBindings, false);
			
			for (Resource anchorResource : cls2Query.keySet()) {
				List<CommandWrapper> commandWrappers = cls2Query.get(anchorResource);
				
				for (CommandWrapper cmd : commandWrappers) {
					// determine referenced assertions
					Set<Resource> referencedAssertions = commandManager.getEngineCommandAdaptor()
							.getControlCommandAssertions(cmd, initialTemplateBindings.get(cmd));
					
					commandRules.add(new CommandRule(cmdProp, anchorResource, cmd, 
							initialTemplateBindings.get(cmd), referencedAssertions));
				}
			}
			
			commandRuleMap.put(cmdProp, commandRules);
		}
    }

	/* We assume we are dealing with an acyclic graph of partial orderings */
	private List<OntProperty> orderCommmandRules(List<? extends OntProperty> ruleProperties) {
	    List<OntProperty> orderedCommandRuleProperties = new LinkedList<OntProperty>();
		
	    Set<OntProperty> visited = new HashSet<OntProperty>();
		List<OntProperty> unordered = new LinkedList<OntProperty>();
	    
		// first determine properties which have no incoming nextRuleProperty relation
		Set<OntProperty> startSet = new HashSet<OntProperty>();
		for (OntProperty r : ruleProperties) {
			if (JenaUtil.getAllTransitiveSubjects(r, SPIN.nextRuleProperty, null).isEmpty()) {
				startSet.add(r);
			}
		}
		
	    // then do DFS to arrange those properties which need to be ordered
	    for (OntProperty ruleProp : startSet) {
	    	if (ruleProp.hasProperty(SPIN.nextRuleProperty)) {
	    		visit(ruleProp, orderedCommandRuleProperties, visited);
	    	}
	    	else {
	    		unordered.add(ruleProp);
	    	}
	    }
	    
	    // put the unordered properties first in the list
	    orderedCommandRuleProperties.addAll(0, unordered);
	    
	    return orderedCommandRuleProperties;
    }

	private void visit(OntProperty ruleProp, List<OntProperty> properties, Set<OntProperty> visited) {
	    // insert in ordered list
		properties.add(ruleProp);
		
		// list next properties
		StmtIterator it = ruleProp.listProperties(SPIN.nextRuleProperty);
		for (; it.hasNext(); ) {
			OntProperty prop = it.next().getResource().as(OntProperty.class);
			if (!visited.contains(prop)) {
				visit(prop, properties, visited);
			}
		}
		
		// mark ruleProp as visited
		visited.add(ruleProp);
    }
}
