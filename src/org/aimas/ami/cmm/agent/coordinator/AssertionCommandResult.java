package org.aimas.ami.cmm.agent.coordinator;

import com.hp.hpl.jena.rdf.model.Resource;

public abstract class AssertionCommandResult extends CommandResult {
	protected Resource assertionResource;
	
	protected AssertionCommandResult(Resource assertionResource) {
		this.assertionResource = assertionResource;
	}
	
	public Resource getAssertionResource() {
		return assertionResource;
	}
}
