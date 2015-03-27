package org.aimas.ami.cmm.agent.coordinator;

import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.util.leap.List;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.coordinator.ContextUpdateManager.SensorDescription;
import org.aimas.ami.cmm.agent.onto.AssertionCapability;
import org.aimas.ami.cmm.agent.onto.AssertionDescription;
import org.aimas.ami.cmm.agent.onto.EnableAssertions;
import org.aimas.ami.cmm.agent.onto.StartSending;
import org.aimas.ami.cmm.agent.onto.impl.DefaultAssertionDescription;
import org.aimas.ami.cmm.agent.onto.impl.DefaultStartSending;
import org.aimas.ami.contextrep.engine.api.CommandHandler;
import org.aimas.ami.contextrep.engine.api.StatsHandler;
import org.aimas.ami.contextrep.model.ContextAssertion.ContextAssertionType;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class EnableAssertionResponder extends AchieveREResponder {
    private static final long serialVersionUID = 8536192752761816871L;
    
    private CtxCoord coordAgent;
	
	@SuppressWarnings("serial")
    private static MessageTemplate prepareTemplate(final CtxCoord coordAgent) {
	    return new MessageTemplate(new MatchExpression() {
			@Override
			public boolean match(ACLMessage msg) {
				if (msg == null) {
					return false;
				}
				
				if (msg.getPerformative() != ACLMessage.REQUEST) 
					return false;
				
				if (!coordAgent.getQueryHandlerManager().isRegistered(msg.getSender()))
					return false;
				
				if (!msg.getOntology().equals(CMMAgent.cmmOntology.getName())) 
					return false;
				
				try {
	                Action contentAction = (Action)coordAgent.getContentManager().extractContent(msg);
	                if (contentAction == null || !(contentAction.getAction() instanceof EnableAssertions)) 
	                	return false;
				}
                catch (Exception e) {
                	return false;
                }
				
				return true;
			}
		});
    }
    
	public EnableAssertionResponder(CtxCoord coordAgent) {
		super(coordAgent, prepareTemplate(coordAgent));
		this.coordAgent = coordAgent;
	}
	
	
	@Override
	protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
		try {
	        CommandHandler engineCommandAdaptor = coordAgent.getCommandManager().getEngineCommandAdaptor();
	        StatsHandler engineStatsAdaptor = coordAgent.getCommandManager().getEngineStatsAdaptor();
	        
	        Action contentAction = (Action)coordAgent.getContentManager().extractContent(request);
			EnableAssertions enableRequest = (EnableAssertions)contentAction.getAction();
	        List requiredAssertions = enableRequest.getEnabledCapability();
	        
	        /*
	        System.out.println("[CtxCoord] INFO: Required Enable Assertions");
	        for (int i = 0; i < requiredAssertions.size(); i++) {
	        	AssertionCapability assertion = (AssertionCapability)requiredAssertions.get(i);
	        	System.out.println(assertion.getAssertion().getAssertionType());
	        }
	        */
	        
	        /* We need to data structures: a list of sensed disabled ContextAssertion
	         * A map (built recursively) for all derived disabled ContextAssertions to the sensed disabled CAs on which they
	         * depend. 
	         */
	        Map<Resource, Boolean> requiredAssertionSatisfied = new HashMap<Resource, Boolean>();
	        Map<Resource, LinkedList<Resource>> requiredDependencyMap = new HashMap<Resource, LinkedList<Resource>>();
	        
	        for (int i = 0; i < requiredAssertions.size(); i++) {
	        	AssertionCapability requiredAssertion = (AssertionCapability)requiredAssertions.get(i);
	        	String requiredAssertionURI = requiredAssertion.getAssertion().getAssertionType();
	        	Resource requiredAssertionRes = ResourceFactory.createResource(requiredAssertionURI);
	        	
	        	//System.out.println("Looking to enable assertion " + requiredAssertionRes);
	        	
	        	ContextAssertionType requiredAssertionCaptureType = engineCommandAdaptor.getAssertionType(requiredAssertionRes);
	        	if (requiredAssertionCaptureType == ContextAssertionType.Profiled) {
	        		engineCommandAdaptor.setAssertionActive(requiredAssertionRes, true);
	        	}
	        	else if (requiredAssertionCaptureType == ContextAssertionType.Sensed) {
	        		requiredAssertionSatisfied.put(requiredAssertionRes, false);
	        	}
	        	else if (requiredAssertionCaptureType == ContextAssertionType.Derived) {
	        		//engineCommandAdaptor.setAssertionActive(requiredAssertionRes, true);
	        		collectDisabledForDerived(requiredAssertionSatisfied, requiredDependencyMap, 
	        				requiredAssertionRes, engineCommandAdaptor, engineStatsAdaptor);
	        	}
	        }
	        
	        // filter through the requiredAssertionSatisfied map and see which ones are sensed, unsatisfied assertions
	        LinkedList<Resource> requiredSensedAssertions = new LinkedList<Resource>();
	        for (Resource assertionRes : requiredAssertionSatisfied.keySet()) {
	        	if (engineCommandAdaptor.getAssertionType(assertionRes) == ContextAssertionType.Sensed) {
	        		requiredSensedAssertions.add(assertionRes);
	        	}
	        }
	        
	        //System.out.println("List of already enabled assertions: " + requiredAssertionSatisfied.keySet());
	        //System.out.println("Final list of needed assertions to enable: " + requiredSensedAssertions);
	        
        	// otherwise we have to enable them by sending requests to the sensors
        	// we register a specific behaviour for this
        	if (!requiredSensedAssertions.isEmpty()) {
        		registerPrepareResultNotification(new EnableUpdatesRequester(coordAgent, request, requiredSensedAssertions, 
        			requiredAssertionSatisfied, requiredDependencyMap));
        	}
		}
        catch (Exception e) {
        	e.printStackTrace();
	        throw new NotUnderstoodException("Enable Assertions message not understood. " + "Reason: " + e.getMessage());
        }
		
		return null;
	}
	
	private void collectDisabledForDerived(Map<Resource, Boolean> requiredAssertionSatisfied,
            Map<Resource, LinkedList<Resource>> requiredDependencyMap,
            Resource requiredAssertionRes, CommandHandler engineCommandAdaptor,
            StatsHandler engineStatsAdaptor) {
	    
		// see which of the referenced assertion are enabled
		LinkedList<Resource> dependencyAssertions = new LinkedList<Resource>();
		requiredDependencyMap.put(requiredAssertionRes, dependencyAssertions);
		
		Set<Resource> referencedAssertions = engineCommandAdaptor.getReferencedAssertions(requiredAssertionRes);
		
		for (Resource referencedAssertion : referencedAssertions) {
			if (!engineStatsAdaptor.getAssertionEnableStatus(referencedAssertion).updatesEnabled()) {
				ContextAssertionType referencedAssertionCaptureType = engineCommandAdaptor.getAssertionType(referencedAssertion);
				
				if (referencedAssertionCaptureType == ContextAssertionType.Profiled) {
					engineCommandAdaptor.setAssertionActive(referencedAssertion, true);
				}
				else if (referencedAssertionCaptureType == ContextAssertionType.Sensed) {
					dependencyAssertions.add(referencedAssertion);
					requiredAssertionSatisfied.put(referencedAssertion, false);
				}
				else if (referencedAssertionCaptureType == ContextAssertionType.Derived &&
						!requiredDependencyMap.containsKey(referencedAssertion)) {
					
					//engineCommandAdaptor.setAssertionActive(referencedAssertion, true);
					dependencyAssertions.add(referencedAssertion);
					collectDisabledForDerived(requiredAssertionSatisfied, requiredDependencyMap, 
							referencedAssertion, engineCommandAdaptor, engineStatsAdaptor);
				}
			}
		}
		
		if (dependencyAssertions.isEmpty()) {
			requiredAssertionSatisfied.put(requiredAssertionRes, true);
			engineCommandAdaptor.setAssertionActive(requiredAssertionRes, true);
		}
		else {
			boolean satisfied = true;
			for (Resource assertionRes : dependencyAssertions) {
				if (!requiredAssertionSatisfied.get(assertionRes)) {
					satisfied = false;
					break;
				}
			}
			
			requiredAssertionSatisfied.put(requiredAssertionRes, satisfied);
			if (satisfied) {
				engineCommandAdaptor.setAssertionActive(requiredAssertionRes, true);
			}
		}
    }
	
	
	@Override
	protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
		return null;
	}
	
	
	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) 
			throws FailureException {
		// If we get here it means we had to deal with a derived assertion activation for which
		// the referenced rule body assertions were already active
		ACLMessage result = request.createReply();
		result.setPerformative(ACLMessage.INFORM);
		
		return result;
	}
	
	
	private static class EnableUpdatesRequester extends AchieveREInitiator {
        private static final long serialVersionUID = -4810111257924711226L;
        private static int counter = 0;
        
		private CtxCoord coordAgent;
		private LinkedList<Resource> requiredAssertions;
		private Map<Resource, Boolean> requiredAssertionSatisfied;
		private Map<Resource, LinkedList<Resource>> requiredDependencyMap;
		private ACLMessage triggerMessage;
		
		private Map<Resource, java.util.List<AID>> requiredSensorAgents;
		
		public EnableUpdatesRequester(CtxCoord coordAgent, ACLMessage triggerMessage,
				LinkedList<Resource> requiredAssertions,
				Map<Resource, Boolean> requiredAssertionSatisfied, 
				Map<Resource, LinkedList<Resource>> requiredDependencyMap) {
	        super(coordAgent, triggerMessage);
	        
	        this.triggerMessage = triggerMessage;
	        this.requiredAssertionSatisfied = requiredAssertionSatisfied;
	        this.requiredDependencyMap = requiredDependencyMap;
	        this.requiredAssertions = requiredAssertions;
	        
	        this.coordAgent = coordAgent;
	        
	        requiredSensorAgents = new HashMap<Resource, java.util.List<AID>>();
        }
		
		@Override
		protected Vector<ACLMessage> prepareRequests(ACLMessage request) {
			Vector<ACLMessage> messageVector = new Vector<ACLMessage>();
			
			for(Resource assertionRes : requiredAssertions) {
				AssertionDescription desc = new DefaultAssertionDescription();
				desc.setAssertionType(assertionRes.getURI());
				
				java.util.List<AID> providers = coordAgent.getContextUpdateManager().getProviders(assertionRes);
				System.out.println("Looking for providers of assertionRes : " + assertionRes);
				
				if (providers != null) {
					requiredSensorAgents.put(assertionRes, providers);
					
					for (AID sensor : providers) {
						StartSending enableRequest = new DefaultStartSending();
						enableRequest.setAssertion(desc);
						ACLMessage enableMessage = createEnableMessage(sensor, enableRequest);
						messageVector.add(enableMessage);
					}
				}
				else {
					System.out.println("No providers for assertion: " + assertionRes);
				}
			}
			
			return messageVector;
		}
		
		private ACLMessage createEnableMessage(AID sensor, StartSending enableRequest) {
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.setLanguage(CMMAgent.cmmCodec.getName());
			msg.setOntology(CMMAgent.cmmOntology.getName());
			
			String conversationId = coordAgent.getName() + "-StartSending-" 
					+ System.currentTimeMillis() + "-" + (counter++);
			msg.setConversationId(conversationId);
			msg.addReceiver(sensor);
			
			try {
				Action enableAction = new Action(coordAgent.getAID(), enableRequest);
				coordAgent.getContentManager().fillContent(msg, enableAction);
            }
            catch (Exception e) {
            	e.printStackTrace();
            }
			
			return msg;
        }
		
		@Override
		protected void handleAllResultNotifications(Vector resultNotifications) {
			// Go through all the responses to the enabling request
			for (int i = 0; i < resultNotifications.size(); i++) {
				ACLMessage resultMessage = (ACLMessage)resultNotifications.get(i);
				
				if (resultMessage.getPerformative() == ACLMessage.INFORM) {
					Resource enabledAssertionRes = extractAssertionResource(resultMessage);
					
					if (enabledAssertionRes != null) {
						// If it is an affirmative reply, add it to the enabled map
						requiredAssertionSatisfied.put(enabledAssertionRes, true);
						
						// and mark it as such in CtxCoordinator sensor state manager for this sensor agent
						SensorDescription sensorDesc = coordAgent.getContextUpdateManager().getSensorDescription(resultMessage.getSender());
						AssertionDescription assertionDesc = sensorDesc.getAssertionByURI(enabledAssertionRes.getURI());
						if (assertionDesc != null) {
							sensorDesc.getAssertionState(assertionDesc).setUpdatesEnabled(true);
						}
					}
				}
			}
			
			// now that we have set the satisfaction state for all received sensed assertions,
			// walk through the derived ones in the satisfaction map and see if they are satisfied as well based on
			// their dependencies
			int n = requiredAssertionSatisfied.size();
			
			// HACK !!!!!
			for (int i = 0; i < n; i++) {
				for (Resource assertionRes : requiredAssertionSatisfied.keySet()) {
					if (!requiredAssertionSatisfied.get(assertionRes)) {
						boolean satisfied = true;
						for (Resource dependencyRes : requiredDependencyMap.get(assertionRes)) {
							if (!requiredAssertionSatisfied.get(dependencyRes)) {
								satisfied = false;
								break;
							}
						}
						
						requiredAssertionSatisfied.put(assertionRes, satisfied);
					}
				}
			}
			
			// In the end, if all are enabled set the RESULT_NOTIFICATION_KEY in the parent behaviour as
			// an INFORM DONE message, otherwise as a failure message.
			// For each required assertion that is enabled, mark this fact in the CONSERT Engine
			boolean allEnabled = true;
			for (Resource requiredAssertionRes : requiredAssertionSatisfied.keySet()) {
				if (!requiredAssertionSatisfied.get(requiredAssertionRes)) {
					allEnabled = false;
				}
				else {
					coordAgent.getCommandManager().getEngineCommandAdaptor().setAssertionActive(requiredAssertionRes, true);
				}
			}
			
			ACLMessage reply = triggerMessage.createReply();
			if (allEnabled) {
				reply.setPerformative(ACLMessage.INFORM);
			}
			else {
				reply.setPerformative(ACLMessage.FAILURE);
			}
			
			AchieveREResponder parent = (AchieveREResponder) getParent();
			getDataStore().put(parent.RESULT_NOTIFICATION_KEY, reply);
		}

		private Resource extractAssertionResource(ACLMessage resultMessage) {
	        try {
	        	Action enableAction = (Action)coordAgent.getContentManager().extractContent(resultMessage);
	        	StartSending enableRequest = (StartSending) enableAction.getAction();
	            String assertionURI = enableRequest.getAssertion().getAssertionType();
	            return ResourceFactory.createResource(assertionURI);
	        }
            catch (Exception e) {
            	e.printStackTrace();
            }
	        
	        return null;
        }
	}
}
