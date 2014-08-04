package org.aimas.ami.cmm.agent.coordinator;

public class InferenceSchedulingCommandResult extends CommandResult {
	private String schedulingType;
	
	public InferenceSchedulingCommandResult(String inferenceSchedulingType) {
		this.schedulingType = inferenceSchedulingType;
	}

	public String getInferenceSchedulingType() {
		return schedulingType;
	}

	@Override
    public boolean conflictsResult(CommandResult otherResult) {
		if (otherResult instanceof InferenceSchedulingCommandResult) {
			InferenceSchedulingCommandResult res = (InferenceSchedulingCommandResult)otherResult;
			return !res.getInferenceSchedulingType().equals(schedulingType);
		}
		
		return false;
    }
}
