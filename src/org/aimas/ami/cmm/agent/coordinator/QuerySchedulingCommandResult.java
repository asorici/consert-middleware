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
}
