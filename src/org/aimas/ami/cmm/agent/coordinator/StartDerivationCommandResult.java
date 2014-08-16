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

	@Override
    public void apply(final CommandManager commandManager) {
	    // For now we can only distinguish between having all derivation commands that derive a particular 
		// ContextAssertion enabled/disabled. In future versions the constructor of this command result subclass 
		// will include the specific DerivationCommand we want to enable, based on, for example, the cost that
		// it takes to carry out the computations.
	    
		// mark the inference for this derived assertion as active in the CONSERT Engine
		commandManager.getEngineCommandAdaptor().setDerivationRuleActive(assertionResource, true);
    }
	
}
