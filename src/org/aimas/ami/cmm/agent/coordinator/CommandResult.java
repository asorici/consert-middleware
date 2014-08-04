package org.aimas.ami.cmm.agent.coordinator;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;

public abstract class CommandResult {

	public static List<CommandResult> createResult(Model newTriples) {
	    // TODO Auto-generated method stub
	    return null;
    }
	
	public abstract boolean conflictsResult(CommandResult otherResult);
}
