package org.aimas.ami.cmm.agent.coordinator;

import com.hp.hpl.jena.rdf.model.Resource;

public class StopDerivationCommandResult extends AssertionCommandResult {
	
	public StopDerivationCommandResult(Resource assertionResource) {
		super(assertionResource);
	}
	
	@Override
	public boolean conflictsResult(CommandResult otherResult) {
		if (otherResult instanceof StartDerivationCommandResult) {
			StartDerivationCommandResult result = (StartDerivationCommandResult)otherResult;
			
			return assertionResource.equals(result.getAssertionResource());
		}
		
		return false;
	}
	
}
