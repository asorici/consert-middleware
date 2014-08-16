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

	@Override
    public void apply(final CommandManager commandManager) {
	    // For now we can only distinguish between having all derivation commands that derive a particular 
		// ContextAssertion enabled/disabled. In future versions the constructor of this command result subclass 
		// will include the specific DerivationCommand we want to disable, based on, for example, the cost that
		// it takes to carry out the computations.
	    
		// mark the inference for this derived assertion as inactive in the CONSERT Engine
		commandManager.getEngineCommandAdaptor().setDerivationRuleActive(assertionResource, false);
    }
	
}
