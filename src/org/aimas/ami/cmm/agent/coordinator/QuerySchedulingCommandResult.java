package org.aimas.ami.cmm.agent.coordinator;

public class QuerySchedulingCommandResult extends CommandResult {
	
	private String schedulingType;
	
	public QuerySchedulingCommandResult(String schedulingType) {
		this.schedulingType = schedulingType;
	}

	public String getQuerySchedulingType() {
		return schedulingType;
	}

	@Override
    public boolean conflictsResult(CommandResult otherResult) {
		if (otherResult instanceof QuerySchedulingCommandResult) {
			QuerySchedulingCommandResult res = (QuerySchedulingCommandResult)otherResult;
			return !res.getQuerySchedulingType().equals(schedulingType);
		}
		
		return false;
    }

	@Override
    public void apply(CommandManager commandManager) {
	    // TODO: we need to create an INFORM message (ontology concept has to be yet created) for all 
		// registered CtxQueryHandlers to notify them that the query scheduling policy has changed.
    }
}
