package org.aimas.ami.cmm.agent.sensor;

import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.lang.acl.ACLMessage;
import jade.proto.ProposeInitiator;

import org.aimas.ami.cmm.agent.onto.AssertionCapability;
import org.aimas.ami.cmm.agent.onto.EnableAssertions;
import org.aimas.ami.cmm.agent.sensor.CtxSensor.SensorState;

public class PublishAssertionsBehaviour extends ProposeInitiator {
    private static final long serialVersionUID = -5902093606245078978L;
    
    private CtxSensor sensorAgent;
    //private PublishAssertions publishContent;
    
	public PublishAssertionsBehaviour(CtxSensor sensorAgent, ACLMessage msg) throws UngroundedException, CodecException, OntologyException {
	    super(sensorAgent, msg);
	    this.sensorAgent = sensorAgent;
	    //this.publishContent = (PublishAssertions)sensorAgent.getContentManager().extractContent(msg);
    }
	
	@Override
	protected void handleAcceptProposal(ACLMessage acceptProposal) {
		/* Since our PublishAssertion messages are 1-to-1, if we get an accept it is the  
		 * only one we will get and it means that the CtxCoordinator is OK with us supplying
		 * updates for the published ContextAssertions. */
		sensorAgent.setSensorState(SensorState.CONNECTED);
		
		// See if the CtxCoordinator has told us to enable anything
		SensingManager sensingManager = sensorAgent.getSensingManager();
		
		Action actionContent = null;
		try {
	        actionContent = (Action)sensorAgent.getContentManager().extractContent(acceptProposal);
	        if (actionContent.getAction() instanceof EnableAssertions) {
	        	EnableAssertions enabledAssertions = (EnableAssertions)actionContent.getAction();
	        	for (int i = 0; i < enabledAssertions.getEnabledCapability().size(); i++) {
	        		AssertionCapability enabledAssertion = (AssertionCapability)enabledAssertions.getEnabledCapability().get(i);
	        		String assertionResURI = enabledAssertion.getAssertion().getAssertionType();
	        		
	        		sensingManager.getAssertionManager(assertionResURI).setUpdateEnabled(true);
	        	}
	        }
        }
        catch (Exception e) {
	        e.printStackTrace();
        }
	}
	
	@Override
	protected void handleRejectProposal(ACLMessage rejectProposal) {
		/* In this initial implementation we do nothing. However, care must be taken to implement
		 * another permanent receiver behaviour that listens for InformAssertions requests from a 
		 * CtxCoordinator. That is, if the CtxCoordinator rejected us initially, he may still want us to
		 * send updates for the managed ContextAssertions in the future. */
	}
}
