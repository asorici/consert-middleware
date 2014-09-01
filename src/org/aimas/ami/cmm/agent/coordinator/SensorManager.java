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
import org.aimas.ami.cmm.exceptions.CMMConfigException;
import org.aimas.ami.contextrep.engine.api.InsertResult;
import org.aimas.ami.contextrep.engine.api.InsertionHandler;
import org.aimas.ami.contextrep.engine.api.InsertionResultNotifier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;


public class SensorManager implements InsertionResultNotifier {
	private CtxCoord coordAgent;
	private InsertionHandler engineInsertionAdaptor;
	
	/* sensor management data structures */
	private Map<AID, SensorDescription> registeredSensors;
	private Map<Resource, List<AID>> assertionProviderMap;
	private List<TaskingCommand> pendingCommands;
	private Map<UpdateRequest, Resource> pendingInsertions;
	
	public SensorManager(CtxCoord ctxCoordinator) throws CMMConfigException {
		this.coordAgent = ctxCoordinator;
		
		registeredSensors = new HashMap<AID, SensorDescription>();
		assertionProviderMap = new HashMap<Resource, List<AID>>();
		pendingCommands = new LinkedList<TaskingCommand>();
		pendingInsertions = new HashMap<UpdateRequest, Resource>();
		
		setupInsertService();
	}
	
	private void setupInsertService() throws CMMConfigException {
		BundleContext context = coordAgent.getOSGiBridge().getBundleContext();
		
		// Get insertion handler
		ServiceReference<InsertionHandler> insertHandlerRef = context.getServiceReference(InsertionHandler.class);
		if (insertHandlerRef == null) {
			throw new CMMConfigException("No reference found for CONSERT Engine service: " + InsertionHandler.class.getName());
		}
		engineInsertionAdaptor = context.getService(insertHandlerRef);
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
	
	public void registerSensor(AID sensorAgent, SensorDescription sensorDescription) {
		registeredSensors.put(sensorAgent, sensorDescription);
		for (AssertionDescription desc : sensorDescription.getProvidedAssertions()) {
			Resource assertionRes = ResourceFactory.createResource(desc.getAssertionType());
			
			List<AID> assertionResProviders = assertionProviderMap.get(assertionRes);
			if (assertionResProviders == null) {
				assertionResProviders = new LinkedList<AID>();
				assertionProviderMap.put(assertionRes, assertionResProviders);
			}
			assertionResProviders.add(sensorAgent);
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
	
	// SENSOR ASSERTION UPDATE
	//////////////////////////////////////////////////////////////////////////////
	public void insertAssertion(AssertionUpdated assertionUpdate) {
		String assertionResURI = assertionUpdate.getAssertion().getAssertionType();
		Resource assertionRes = ResourceFactory.createResource(assertionResURI);
		
		String assertionContent = assertionUpdate.getAssertionContent();
		UpdateRequest updateRequest = UpdateFactory.create(assertionContent, Syntax.syntaxSPARQL_11);
		
		pendingInsertions.put(updateRequest, assertionRes);
		engineInsertionAdaptor.insert(updateRequest, this);
	}
	
	@Override
    public void notifyInsertionResult(InsertResult insertResult) {
		if (insertResult.hasConstraintViolations()) {
	    	System.out.println("OH OH!!! We're in trouuuuble !!!");
	    }
		
		// If no errors occurred, remove the updated Assertion from the pending inserts and notify the 
		// command manager of its update.
		if (!insertResult.hasConstraintViolations() && !insertResult.hasExecError()) {
			Resource updatedAssertionResource = pendingInsertions.remove(insertResult.getInsertRequest());
			if (updatedAssertionResource != null) {
				coordAgent.getCommandManager().notifyAssertionUpdated(updatedAssertionResource);
			}
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
		
		public AssertionState addCapability(AssertionCapability capability) {
			AssertionDescription assertionDesc = capability.getAssertion();
			String updateMode = capability.getAvailableUpdateMode();
			int updateRate = capability.getAvailableUpdateRate();
			
			AssertionState assertionState = new AssertionState(false, updateMode, updateRate);
			providedAssertions.add(assertionDesc);
			providedAssertionsState.put(assertionDesc, assertionState);
			
			return assertionState;
		}
		
		public void addCapability(AssertionDescription assertionDesc, AssertionState assertionState) {
			providedAssertions.add(assertionDesc);
			providedAssertionsState.put(assertionDesc, assertionState);
		}
		
		
		public List<AssertionDescription> getProvidedAssertions() {
			return providedAssertions;
		}

		/**
		 * Retrieve an assertion description based on the given ContextAssertion URI.
		 * If there are more than one, retrieve one at random.
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
