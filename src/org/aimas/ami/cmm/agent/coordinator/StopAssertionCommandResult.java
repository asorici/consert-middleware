package org.aimas.ami.cmm.agent.coordinator;

import com.hp.hpl.jena.rdf.model.Resource;

public class StopAssertionCommandResult extends AssertionCommandResult {
	
	public StopAssertionCommandResult(Resource assertionResource) {
		super(assertionResource);
	}
	
	@Override
	public boolean conflictsResult(CommandResult otherResult) {
		if (otherResult instanceof StartAssertionCommandResult) {
			StartAssertionCommandResult result = (StartAssertionCommandResult)otherResult;
			
			return assertionResource.equals(result.getAssertionResource());
		}
		
		return false;
	}
	
}
