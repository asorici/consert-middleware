package org.aimas.ami.cmm.agent.coordinator;

import com.hp.hpl.jena.rdf.model.Resource;

public class UpdateModeCommandResult extends AssertionCommandResult {
	private String updateMode;
	private int updateRate;
	
	public UpdateModeCommandResult(Resource assertionResource, String updateMode, int updateRate) {
		super(assertionResource);
		
		this.updateMode = updateMode;
		this.updateRate = updateRate;
	}
	
	@Override
	public boolean conflictsResult(CommandResult otherResult) {
		if (otherResult instanceof UpdateModeCommandResult) {
			UpdateModeCommandResult result = (UpdateModeCommandResult)otherResult;
			
			if (result.getAssertionResource().equals(assertionResource)) {
				if (!result.getUpdateMode().equals(updateMode)) {
					return true;
				}
				else if (result.getUpdateRate() != updateRate) {
					return true;
				}
			}
		}
		
		return false;
	}

	public String getUpdateMode() {
		return updateMode;
	}

	public int getUpdateRate() {
		return updateRate;
	}
	
}
