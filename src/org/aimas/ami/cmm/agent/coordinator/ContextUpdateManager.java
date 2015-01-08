package org.aimas.ami.cmm.agent.coordinator;

import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.onto.AssertionCapability;
import org.aimas.ami.cmm.agent.onto.AssertionDescription;
import org.aimas.ami.cmm.agent.onto.AssertionUpdated;
import org.aimas.ami.cmm.agent.onto.UpdateEntityDescriptions;
import org.aimas.ami.cmm.agent.onto.UpdateProfiledAssertion;
import org.aimas.ami.cmm.api.CMMConfigException;
import org.aimas.ami.cmm.sensing.ContextAssertionAdaptor;
import org.aimas.ami.contextrep.engine.api.InsertResult;
import org.aimas.ami.contextrep.engine.api.InsertionHandler;
import org.aimas.ami.contextrep.engine.api.InsertionResultNotifier;
import org.aimas.ami.contextrep.engine.api.StatsHandler.AssertionEnableStatus;

import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;


public class ContextUpdateManager implements InsertionResultNotifier {
	private CtxCoord coordAgent;
	private InsertionHandler engineInsertionAdaptor;
	
	/* sensor management data structures */
	private Map<AID, SensorDescription> registeredSensors;
	private Map<Resource, List<AID>> assertionProviderMap;
	private List<TaskingCommand> pendingCommands;
	private Map<UpdateRequest, Resource> pendingInsertions;
	
	public ContextUpdateManager(CtxCoord ctxCoordinator, InsertionHandler engineInsertionAdaptor) throws CMMConfigException {
		this.coordAgent = ctxCoordinator;
		
		registeredSensors = new HashMap<AID, SensorDescription>();
		assertionProviderMap = new HashMap<Resource, List<AID>>();
		pendingCommands = new LinkedList<TaskingCommand>();
		pendingInsertions = new HashMap<UpdateRequest, Resource>();
		
		this.engineInsertionAdaptor = engineInsertionAdaptor;
	}
	
	
	// MANAGEMENT METHODS 
	/////////////////////////////////////////////////////////////////////////////
	public void submitCommand(AID ctxSensor, TaskingCommand taskingCommand) {
		pendingCommands.add(taskingCommand);
		coordAgent.addBehaviour(new TaskingBehaviour(coordAgent, ctxSensor, taskingCommand));
	}
	
	public void removeCommand(TaskingCommand taskingCommand) {
		pendingCommands.remove(taskingCommand);
	}
	
	public void registerOrUpdateSensor(AID sensorAgent, SensorDescription sensorDescription) {
		boolean update = registeredSensors.containsKey(sensorAgent);
		
		if (!update) {
			registeredSensors.put(sensorAgent, sensorDescription);
		}
		
		for (AssertionDescription desc : sensorDescription.getProvidedAssertions()) {
			Resource assertionRes = ResourceFactory.createResource(desc.getAssertionType());
			
			List<AID> assertionResProviders = assertionProviderMap.get(assertionRes);
			if (assertionResProviders == null) {
				assertionResProviders = new LinkedList<AID>();
				assertionProviderMap.put(assertionRes, assertionResProviders);
			}
			
			if (update) {
				if (!assertionResProviders.contains(sensorAgent)) {
					assertionResProviders.add(sensorAgent);
				}
			}
			else {
				assertionResProviders.add(sensorAgent);
			}
		}
	}
	
	
	/**
	 * Try to find an existing assertion state for a ContextAssertion description.
	 * This method returns the first one found, or null if none exists among the 
	 * registered CtxSensors. This is based on the idea that in the current version we assume
	 * that a ContextAssertion Description is configured the same way for all provider agents.
	 * @param assertionDesc
	 * @return
	 */
	public AssertionState matchDescription(AssertionDescription assertionDesc) {
		for (SensorDescription sensorDesc : registeredSensors.values()) {
			if (sensorDesc.getAssertionState(assertionDesc) != null) {
				return sensorDesc.getAssertionState(assertionDesc);
			}
		}
		
		return null;
	}
	
	
	// GETTERS 
	/////////////////////////////////////////////////////////////////////////////
	public SensorDescription getSensorDescription(AID sensorAgent) {
		return registeredSensors.get(sensorAgent);
	}
	
	public Set<AID> getRegisteredSensors() {
		return registeredSensors.keySet();
	}
	
	public boolean isRegistered(AID sensorAgent) {
		return registeredSensors.containsKey(sensorAgent);
	}
	
	/**
	 * Retrieve the list of registered agents that can provide the ContextAssertion identified
	 * by the <code>assertionResource</code>.
	 * @param assertionResource
	 * @return
	 */
	public List<AID> getProviders(Resource assertionResource) {
		return assertionProviderMap.get(assertionResource);
	}
	
	// USER ENTITY DESCRIPTION + PROFILED ASSERTION UPDATE MANAGEMENT
	//////////////////////////////////////////////////////////////////////////////
	public void updateEntityDescriptions(UpdateEntityDescriptions entitiesUpdate) {
		String updateRequestString = entitiesUpdate.getEntityContents();
        UpdateRequest updateRequest = UpdateFactory.create(updateRequestString, Syntax.syntaxSPARQL_11);
        
        engineInsertionAdaptor.updateEntityDescriptions(updateRequest);
	}
	
	
	public boolean updateProfiledAssertion(UpdateProfiledAssertion profiledAssertionUpdate) {
		AssertionDescription assertionDesc = profiledAssertionUpdate.getAssertion();
		String updateRequestString = profiledAssertionUpdate.getAssertionContent();
		
        UpdateRequest updateRequest = UpdateFactory.create(updateRequestString, Syntax.syntaxSPARQL_11);
        Resource assertionRes = ResourceFactory.createResource(assertionDesc.getAssertionType());
        
        AssertionEnableStatus status = coordAgent.getCommandManager().getEngineStatsAdaptor()
        		.getAssertionEnableStatus(assertionRes);
        
        if (status.updatesEnabled()) {
        	pendingInsertions.put(updateRequest, assertionRes);
        	engineInsertionAdaptor.updateProfiledAssertion(updateRequest, this);
        	return true;
        }
        
        return false;
	}
	
	// SENSOR ASSERTION UPDATE
	//////////////////////////////////////////////////////////////////////////////
	public void insertAssertion(AID sensorAgent, AssertionUpdated assertionUpdate) {
		String assertionResURI = assertionUpdate.getAssertion().getAssertionType();
		Resource assertionRes = ResourceFactory.createResource(assertionResURI);
		
		// get the sensor description of the sender
		SensorDescription sensorDesc = registeredSensors.get(sensorAgent);
		
		// get the AssertionState for the ContextAssertion being updated and see in which type of update-mode it is
		// we ASSUME that a same CtxSensor will not send several versions of the same ContextAssertion, since the
		// CONSERT Engine can currently not distinguish between such versions.
		AssertionDescription assertionDesc = sensorDesc.getAssertionByURI(assertionResURI);
		AssertionState assertionState = sensorDesc.getAssertionState(assertionDesc);
		
		int updateMode = assertionState.getUpdateMode().equalsIgnoreCase(ContextAssertionAdaptor.TIME_BASED) 
				? InsertionHandler.TIME_BASED_UPDATE_MODE : InsertionHandler.CHANGE_BASED_UPDATE_MODE; 
		
		// retrieve the assertion update content and create the Jena UpdateRequest
		String assertionContent = assertionUpdate.getAssertionContent();
		UpdateRequest updateRequest = UpdateFactory.create(assertionContent, Syntax.syntaxSPARQL_11);
		
		// mark insertion as pending (awaiting notification of insertion) and submit to CONSERT Engine
		pendingInsertions.put(updateRequest, assertionRes);
		engineInsertionAdaptor.insertAssertion(updateRequest, this, updateMode);
	}
	
	@Override
    public void notifyInsertionResult(InsertResult insertResult) {
		/* For now, the only thing we do in the ContextAssertion result notification handler
		 * is to remove the insertion form pending status and notify the commandManager that there has been
		 * an update of the particular ContextAssertion. 
		 * It remains to be seen if the update process HAS to be an ASYNCHRONOUS TWO-WAY PROCESS, i.e. if the
		 * CtxSensor or CtxUser have to be notified of the insertion result. 
		 * Intuition says that in the case of Context Level Agreements it could be useful for the providers to
		 * know IF (i.e. were there any errors or constraint violations) and HOW FAST their update was processed. */
		
		// Remove the updated Assertion from the pending inserts.
		// If no errors occurred, notify the command manager of its update.
		Resource updatedAssertionResource = pendingInsertions.remove(insertResult.getInsertRequest());
		if (!insertResult.hasConstraintViolations() && !insertResult.hasExecError() 
				&& updatedAssertionResource != null) {
			coordAgent.getCommandManager().notifyAssertionUpdated(updatedAssertionResource);
		}
    }
	
	// INTERNAL SENSOR MANAGEMENT
	//////////////////////////////////////////////////////////////////////////////
	static class SensorDescription {
		private List<AssertionDescription> providedAssertions;
		private Map<AssertionDescription, AssertionState> providedAssertionsState;
		
		SensorDescription() {
			providedAssertions = new LinkedList<AssertionDescription>();
			providedAssertionsState = new HashMap<AssertionDescription, AssertionState>();
		}
		
		public AssertionState addOrUpdateCapability(AssertionCapability capability) {
			AssertionDescription assertionDesc = capability.getAssertion();
			String updateMode = capability.getAvailableUpdateMode();
			int updateRate = capability.getAvailableUpdateRate();
			
			// First check to see if this assertion description already exists
			AssertionState assertionState = getAssertionState(assertionDesc);
			if (assertionState == null) {
				AssertionDescription existingDescription = getAssertionByURI(assertionDesc.getAssertionType());  
				
				// If the description differs, but the assertion type is the same => we have an update
				if (existingDescription != null) {
					// Replace the current description
					providedAssertions.remove(existingDescription);
					providedAssertions.add(assertionDesc);
					
					// Get the current state of the existing description and remove the mapping
					AssertionState existingState = providedAssertionsState.remove(existingDescription);
					if (!existingState.isUpdatesEnabled()) {
						// If updates are not enabled, set the new state as given by the assertion capability
						assertionState = new AssertionState(false, updateMode, updateRate);
						providedAssertionsState.put(assertionDesc, assertionState);
					}
					else {
						// Updates are enabled, meaning that the desired update mode is already configured by this
						// coordinator. A CHANGE IN UPDATE MODE REQUESTED BY THE CtxSensor AGENT IS HANDLED
						// HANDLED IN ANOTHER BEHAVIOR (TODO).
						// Here we just replace the previous mapping, following the description change
						providedAssertionsState.put(assertionDesc, existingState);
					}
				}
				else {
					// There is no existing assertion type => new capability
					assertionState = new AssertionState(false, updateMode, updateRate);
					providedAssertions.add(assertionDesc);
					providedAssertionsState.put(assertionDesc, assertionState);
				}
			}
			else {
				// If the AssertionDescription already exists, we will only update the state if updates are
				// not currently enabled
				if (!assertionState.isUpdatesEnabled()) {
					assertionState.setUpdateMode(updateMode);
					assertionState.setUpdateRate(updateRate);
				}
			}
			
			return assertionState;
		}
		
		
		public List<AssertionDescription> getProvidedAssertions() {
			return providedAssertions;
		}

		/**
		 * Retrieve the assertion description based on the given ContextAssertion URI.
		 * @param assertionURI
		 * @return An assertion description matching the given ContextAssertion resource URI, 
		 *  or null if no such assertion exists
		 */
		AssertionDescription getAssertionByURI(String assertionURI) {
			for (AssertionDescription desc : providedAssertions) {
				if (desc.getAssertionType().equals(assertionURI)) {
					return desc;
				}
			}
			
			return null;
		}
		
		/**
		 * Retrieve all assertion descriptions matching the given ContextAssertion URI.
		 * @param assertionURI
		 */
		List<AssertionDescription> listAssertionsByURI(String assertionURI) {
			List<AssertionDescription> filtered = new LinkedList<AssertionDescription>();
			for (AssertionDescription desc : providedAssertions) {
				if (desc.getAssertionType().equals(assertionURI)) {
					filtered.add(desc);
				}
			}
			
			return filtered;
		}
		
		AssertionState getAssertionState(AssertionDescription desc) {
			return providedAssertionsState.get(desc);
		}
	}
	
	
	static class AssertionState {
		private boolean updatesEnabled;
		private String updateMode;
		private int updateRate;
		
		AssertionState(boolean updatesEnabled, String updateMode, int updateRate) {
	        this.updatesEnabled = updatesEnabled;
	        this.updateMode = updateMode;
	        this.updateRate = updateRate;
        }
		
		public boolean isUpdatesEnabled() {
			return updatesEnabled;
		}
		
		public void setUpdatesEnabled(boolean updatesEnabled) {
			this.updatesEnabled = updatesEnabled;
		}
		
		public String getUpdateMode() {
			return updateMode;
		}
		
		public void setUpdateMode(String updateMode) {
			this.updateMode = updateMode;
		}
		
		public int getUpdateRate() {
			return updateRate;
		}
		
		public void setUpdateRate(int updateRate) {
			this.updateRate = updateRate;
		}
	}
	
	
	// TASKING COMMAND BEHAVIOUR
	//////////////////////////////////////////////////////////////////////////////
	private class TaskingBehaviour extends SimpleAchieveREInitiator {
        private static final long serialVersionUID = -7173337093124365996L;
        private static final int TASKING_TIMEOUT = 10000;
        
		private TaskingCommand taskingCommand;
		private CtxCoord ctxCoord;
		private AID ctxSensor;
		
		public TaskingBehaviour(CtxCoord coordinator, AID ctxSensor, TaskingCommand task) {
	        super(coordinator, null);
	        
	        this.ctxCoord = coordinator;
	        this.ctxSensor = ctxSensor;
	        this.taskingCommand = task;
        }
		
		protected ACLMessage prepareRequest(ACLMessage msg) {
			ACLMessage taskRequest = new ACLMessage(ACLMessage.REQUEST);
			taskRequest.addReceiver(ctxSensor);
			taskRequest.setLanguage(CMMAgent.cmmCodec.getName());
			taskRequest.setOntology(CMMAgent.cmmOntology.getName());
			taskRequest.setProtocol(InteractionProtocol.FIPA_REQUEST);
			
			// set conversation id
			String conversationId = ctxCoord.getName() + "-tasking-command-" + System.currentTimeMillis() + getCnt();
			taskRequest.setConversationId(conversationId);
			
			// set reply deadling
			long deadline = CMMAgent.currentTimeMillis() + TASKING_TIMEOUT;
			Date deadlineDate = new Date(deadline);
			taskRequest.setReplyByDate(deadlineDate);
			
			try {
				Action taskingCommandAction = new Action(ctxSensor, taskingCommand.getTask());
	            ctxCoord.getContentManager().fillContent(taskRequest, taskingCommandAction);
            }
            catch (Exception e) {}
			
			return taskRequest;
		}
		
		@Override
		protected void handleNotUnderstood(ACLMessage msg) {
			taskingCommand.handleFailure(msg);
			removeCommand(taskingCommand);
	    }
		
		@Override
		protected void handleRefuse(ACLMessage msg) {
			taskingCommand.handleFailure(msg);
			removeCommand(taskingCommand);
		}
		
		@Override
		protected void handleFailure(ACLMessage msg) {
			taskingCommand.handleFailure(msg);
			removeCommand(taskingCommand);
		}
		
		@Override
		protected void handleInform(ACLMessage msg) {
			if (msg != null) {
				// If deadline not passed
				taskingCommand.handleSuccess(msg);
			}
			else {
				taskingCommand.handleFailure(msg);
			}
			
			removeCommand(taskingCommand);
		}
	}
	
	private static int counter = 0;
	private static synchronized int getCnt() {
		return counter++;
	}
}
