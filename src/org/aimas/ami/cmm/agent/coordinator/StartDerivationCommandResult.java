package org.aimas.ami.cmm.agent.coordinator;

import com.hp.hpl.jena.rdf.model.Resource;

public class StartDerivationCommandResult extends AssertionCommandResult {
	
	public StartDerivationCommandResult(Resource assertionResource) {
		super(assertionResource);
	}
	
	@Override
	public boolean conflictsResult(CommandResult otherResult) {
		if (otherResult instanceof StopDerivationCommandResult) {
			StopDerivationCommandResult result = (StopDerivationCommandResult)otherResult;
			
			return assertionResource.equals(result.getAssertionResource());
		}
		
		return false;
	}
	
}
