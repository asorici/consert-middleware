package org.aimas.ami.cmm.agent.coordinator;

import com.hp.hpl.jena.rdf.model.Resource;

public class StartAssertionCommandResult extends AssertionCommandResult {

	protected StartAssertionCommandResult(Resource assertionResource) {
	    super(assertionResource);
    }

	@Override
    public boolean conflictsResult(CommandResult otherResult) {
		if (otherResult instanceof StopAssertionCommandResult) {
			StopAssertionCommandResult result = (StopAssertionCommandResult)otherResult;
			
			return assertionResource.equals(result.getAssertionResource());
		}
		
		return false;
    }
}
