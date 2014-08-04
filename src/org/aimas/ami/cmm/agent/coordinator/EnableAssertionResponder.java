package org.aimas.ami.cmm.agent.coordinator;

import jade.content.ContentElement;
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
				
				if (!msg.getOntology().equals(coordAgent.getCMMOntology().getName())) 
					return false;
				
				try {
	                ContentElement ce = coordAgent.getContentManager().extractContent(msg);
	                if (ce == null || !(ce instanceof EnableAssertions)) 
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
	        
			EnableAssertions enableRequest = (EnableAssertions)coordAgent.getContentManager().extractContent(request);
	        List requiredAssertions = enableRequest.getCapability();
	        
	        for (int i = 0; i < requiredAssertions.size(); i++) {
	        	AssertionCapability requiredAssertion = (AssertionCapability)requiredAssertions.get(i);
	        	String requiredAssertionURI = requiredAssertion.getAssertion().getAssertionType();
	        	Resource requiredAssertionRes = ResourceFactory.createResource(requiredAssertionURI);
	        	
	        	LinkedList<Resource> referencedDisabled = new LinkedList<Resource>();
	        	
	        	if (engineCommandAdaptor.getAssertionType(requiredAssertionRes) == ContextAssertionType.Derived) {
	        		// see which of the referenced assertion are enabled
	        		Set<Resource> referencedAssertions = engineCommandAdaptor.getReferencedAssertions(requiredAssertionRes);
	        		for (Resource referencedAssertion : referencedAssertions) {
	        			if (!engineStatsAdaptor.assertionUpdatesEnabled(referencedAssertion)) {
	        				referencedDisabled.add(referencedAssertion);
	        			}
	        		}
	        	}
	        	
	        	if (referencedDisabled.isEmpty()) {
	        		// if there are no disabled referenced assertions - just enable the derived one
	        		engineCommandAdaptor.setAssertionActive(requiredAssertionRes, true);
	        	}
	        	else {
	        		// otherwise we have to enable them by sending requests to the sensors
	        		// we register a specific behaviour for this
	        		registerPrepareResultNotification(new EnableUpdatesRequester(coordAgent, request, referencedDisabled));
	        	}
	        	
	        }
		}
        catch (Exception e) {
	        throw new NotUnderstoodException("Enable Assertions message not understood. "
	        		+ "Reason: " + e.getMessage());
        }
		
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
		private ACLMessage triggerMessage;
		
		private Map<Resource, java.util.List<AID>> requiredSensorAgents;
		
		public EnableUpdatesRequester(CtxCoord coordAgent, ACLMessage triggerMessage, LinkedList<Resource> requiredAssertions) {
	        super(coordAgent, triggerMessage);
	        
	        this.triggerMessage = triggerMessage;
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
				
				java.util.List<AID> providers = coordAgent.getSensorManager().getProviders(assertionRes);
				requiredSensorAgents.put(assertionRes, providers);
				
				for (AID sensor : providers) {
					StartSending enableRequest = new DefaultStartSending();
					ACLMessage enableMessage = createEnableMessage(sensor, enableRequest);
					messageVector.add(enableMessage);
				}
			}
			
			return messageVector;
		}
		
		private ACLMessage createEnableMessage(AID sensor, StartSending enableRequest) {
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.setLanguage(coordAgent.getCMMCodec().getName());
			msg.setOntology(coordAgent.getCMMOntology().getName());
			
			String conversationId = coordAgent.getName() + "-StartSending-" 
					+ System.currentTimeMillis() + "-" + (counter++);
			msg.setConversationId(conversationId);
			msg.addReceiver(sensor);
			
			try {
	            coordAgent.getContentManager().fillContent(msg, enableRequest);
            }
            catch (Exception e) {}
			
			return msg;
        }
		
		@Override
		protected void handleAllResultNotifications(Vector resultNotifications) {
			Map<Resource, Boolean> enablingMap = new HashMap<Resource, Boolean>();
			
			for (int i = 0; i < resultNotifications.size(); i++) {
				ACLMessage resultMessage = (ACLMessage)resultNotifications.get(i);
				
				if (resultMessage.getPerformative() == ACLMessage.INFORM) {
					Resource enabledAssertionRes = extractAssertionResource(resultMessage);
					
					if (enabledAssertionRes != null) {
						enablingMap.put(enabledAssertionRes, true);
					}
				}
			}
			
			// in the end, if all are enabled set the RESULT_NOTIFICATION_KEY in the parent behaviour as
			// an INFORM DONE message, otherwise as a failure message
			boolean allEnabled = true;
			for (Resource requiredAssertionRes : requiredAssertions) {
				if (!enablingMap.get(requiredAssertionRes)) {
					allEnabled = false;
					break;
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
	            StartSending enableRequest = (StartSending) coordAgent.getContentManager().extractContent(resultMessage);
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
