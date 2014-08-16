package org.aimas.ami.cmm.agent.coordinator;

import java.util.LinkedList;
import java.util.List;

import org.aimas.ami.cmm.vocabulary.CoordConf;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public abstract class CommandResult {

	public static List<CommandResult> createResult(Model newTriples) {
	    // The creation process is simply a list of attempts to
		// detect each type of TaskingCommand
		List<CommandResult> commandRuleResults = new LinkedList<CommandResult>();
		
		// check for StartAssertion Commands
		commandRuleResults.addAll(getStartAssertionCommands(newTriples));
		
		// check for StopAssertion Commands
		commandRuleResults.addAll(getStopAssertionCommands(newTriples));
		
		// check for StartDerivation Commands
		commandRuleResults.addAll(getStartDerivationCommands(newTriples));
		
		// check for StopDerivation Commands
		commandRuleResults.addAll(getStopDerivationCommands(newTriples));
		
		// check for UpdateMode Commands
		commandRuleResults.addAll(getUpdateModeCommands(newTriples));
		
		// check for InferenceScheduling Commands
		commandRuleResults.addAll(getInferenceScheduleCommands(newTriples));
		
		// check for QueryScheduling Commands
		commandRuleResults.addAll(getQueryScheduleCommands(newTriples));
		
	    return commandRuleResults;
    }
	
	private static List<CommandResult> getStartAssertionCommands(Model newTriples) {
	    List<CommandResult> results = new LinkedList<CommandResult>();
	    
	    ResIterator resultIt = newTriples.listSubjectsWithProperty(RDF.type, CoordConf.StartAssertionCommand);
	    for (; resultIt.hasNext(); ) {
	    	Resource commandRes = resultIt.next();
	    	Resource assertionResource = commandRes.getPropertyResourceValue(CoordConf.forContextAssertion);
	    	
	    	results.add(new StartAssertionCommandResult(assertionResource));
	    }
	    
	    return results;
    }
	
	private static List<CommandResult> getStopAssertionCommands(Model newTriples) {
		List<CommandResult> results = new LinkedList<CommandResult>();
	    
	    ResIterator resultIt = newTriples.listSubjectsWithProperty(RDF.type, CoordConf.StopAssertionCommand);
	    for (; resultIt.hasNext(); ) {
	    	Resource commandRes = resultIt.next();
	    	Resource assertionResource = commandRes.getPropertyResourceValue(CoordConf.forContextAssertion);
	    	
	    	results.add(new StopAssertionCommandResult(assertionResource));
	    }
	    
	    return results;
    }
	
	private static List<CommandResult> getStartDerivationCommands(Model newTriples) {
		List<CommandResult> results = new LinkedList<CommandResult>();
	    
	    ResIterator resultIt = newTriples.listSubjectsWithProperty(RDF.type, CoordConf.StartRuleCommand);
	    for (; resultIt.hasNext(); ) {
	    	Resource commandRes = resultIt.next();
	    	Resource assertionResource = commandRes.getPropertyResourceValue(CoordConf.forContextAssertion);
	    	
	    	results.add(new StartDerivationCommandResult(assertionResource));
	    }
	    
	    return results;
	}
	
	private static List<CommandResult> getStopDerivationCommands(Model newTriples) {
		List<CommandResult> results = new LinkedList<CommandResult>();
	    
	    ResIterator resultIt = newTriples.listSubjectsWithProperty(RDF.type, CoordConf.StopRuleCommand);
	    for (; resultIt.hasNext(); ) {
	    	Resource commandRes = resultIt.next();
	    	Resource assertionResource = commandRes.getPropertyResourceValue(CoordConf.forContextAssertion);
	    	
	    	results.add(new StopDerivationCommandResult(assertionResource));
	    }
	    
	    return results;
    }
	
	private static List<CommandResult> getUpdateModeCommands(Model newTriples) {
		List<CommandResult> results = new LinkedList<CommandResult>();
	    
	    ResIterator resultIt = newTriples.listSubjectsWithProperty(RDF.type, CoordConf.StopRuleCommand);
	    for (; resultIt.hasNext(); ) {
	    	Resource commandRes = resultIt.next();
	    	Resource assertionResource = commandRes.getPropertyResourceValue(CoordConf.forContextAssertion);
	    	String updateMode = commandRes.getPropertyResourceValue(CoordConf.setsUpdateMode).getLocalName();
	    	int updateRate = commandRes.getProperty(CoordConf.setsTransferRate).getInt();
	    	
	    	results.add(new UpdateModeCommandResult(assertionResource, updateMode, updateRate));
	    }
	    
	    return results;
    }
	
	private static List<CommandResult> getInferenceScheduleCommands(Model newTriples) {
		List<CommandResult> results = new LinkedList<CommandResult>();
	    
	    ResIterator resultIt = newTriples.listSubjectsWithProperty(RDF.type, CoordConf.InferenceSchedulingCommand);
	    for (; resultIt.hasNext(); ) {
	    	Resource commandRes = resultIt.next();
	    	String schedulingType = commandRes.getPropertyResourceValue(CoordConf.setsSchedulingType).getLocalName();
	    	
	    	results.add(new InferenceSchedulingCommandResult(schedulingType));
	    }
	    
	    return results;
    }
	
	private static List<CommandResult> getQueryScheduleCommands(Model newTriples) {
		List<CommandResult> results = new LinkedList<CommandResult>();
	    
	    ResIterator resultIt = newTriples.listSubjectsWithProperty(RDF.type, CoordConf.QuerySchedulingCommand);
	    for (; resultIt.hasNext(); ) {
	    	Resource commandRes = resultIt.next();
	    	String schedulingType = commandRes.getPropertyResourceValue(CoordConf.setsSchedulingType).getLocalName();
	    	
	    	results.add(new QuerySchedulingCommandResult(schedulingType));
	    }
	    
	    return results;
    }
	
	public abstract boolean conflictsResult(CommandResult otherResult);
	
	public abstract void apply(CommandManager commandManager);
}
