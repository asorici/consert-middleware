package org.aimas.ami.cmm.agent.sensor;

import jade.content.ContentElement;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;

import java.util.Map;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.config.SensorSpecification;
import org.aimas.ami.cmm.agent.onto.AssertionCapability;
import org.aimas.ami.cmm.agent.onto.AssertionDescription;
import org.aimas.ami.cmm.agent.onto.ExecTask;
import org.aimas.ami.cmm.agent.onto.PublishAssertions;
import org.aimas.ami.cmm.agent.onto.impl.DefaultAssertionCapability;
import org.aimas.ami.cmm.agent.onto.impl.DefaultPublishAssertions;
import org.aimas.ami.cmm.exceptions.CMMConfigException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

public class CtxSensor extends CMMAgent {
    private static final long serialVersionUID = 4254689617972269011L;
    
    public static enum SensorState {
    	INACTIVE, ACTIVE, CONNECTED, TRANSMITTING
    }
    
    /* Application unique identifier and agent specification */
    private String appIdentifier;
    private SensorSpecification sensorSpecification;
    
    /* SensingManager, CtxSensor state and the CtxCoord currently connected to (there will be more 
     * in the extended decentralized hierarchical version) */
    private SensingManager sensedAssertionsManager;
    private SensorState sensorState = SensorState.INACTIVE;
    
    
    SensorState getSensorState() {
    	return sensorState;
    }
    
    void setSensorState(SensorState state) {
    	this.sensorState = state;
    }
    
    SensingManager getSensingManager() {
    	return sensedAssertionsManager;
    }
    
    SensorSpecification getSensorSpecification() {
    	return sensorSpecification;
    }
    
    @Override
    public void setup() {
    	// ======== STEP 1:	retrieve the initialization arguments and set CMM agent language
    	Object[] initArgs = getArguments();
    	
    	// first argument is always the URI of the AgentSpecification resource in the 
    	String agentSpecURI = (String)initArgs[0];
    	
    	// second one is always the application unique identifier
    	appIdentifier = (String)initArgs[1];
    	
    	// third one is optional and denotes the AID of a local OrgMgr
    	if (initArgs.length == 3) {
    		localOrgMgr = (AID)initArgs[2];
    	}
    	
    	// register the CMMAgent-Lang ontology
    	registerCMMAgentLang();
    	
    	// ======== STEP 2a: configure the agent according to its specification	
    	try {
	    	// configure access to resource
    		doResourceAccessConfiguration();
    		
    		OntModel cmmConfigModel = configurationLoader.loadAppConfiguration();
    		Resource agentSpecRes = cmmConfigModel.getResource(agentSpecURI);
    		
    		if (agentSpecRes == null) {
    			throw new CMMConfigException("No CtxSensor specification found in configuration model "
    					+ "for URI " + agentSpecURI);
    		}
    		
    		// retrieve specification and create sensed assertions manager
    		agentSpecification = SensorSpecification.fromConfigurationModel(cmmConfigModel, agentSpecRes);
    		sensorSpecification = (SensorSpecification)agentSpecification;
    		sensedAssertionsManager = new SensingManager(this, sensorSpecification.getSensingPolicies());
    		
    		// after this step initialization of the CtxSensor is complete, so we signal a successful init
    		signalInitializationOutcome(true);
    		
    		// last thing we do in this step is switch our state to ACTIVE
    		sensorState = SensorState.ACTIVE; 
    	}
    	catch(CMMConfigException e) {
    		// if we have a local OrgMgr we must signal our initialization failure
    		signalInitializationOutcome(false);
    	}
    	
    	// ======== STEP 2b: if there is a local OrgMgr register with it as part of the DF
    	// A CMM agent will specify two attributes in its service description: app-name
    	// and agent-type.
    	registerSensorService();
    	
    	// ======== STEP 3:	setup the CtxSensor specific permanent behaviours
    	setupSensorBehaviours();
    	
    	// ======== STEP 4: connect to the CtxCoord by publishing the proposal to supply updates
    	// of the sensed ContextAssertions. This step may in fact be more complex, when we will operate
    	// in a decentralized hierarchical - multi-coordinator setting, but for this first version
    	// we only need to connect to the specified CtxCoord
    	doConnectProposal();
    	
    }
    
    /**
     * If there is a local OrgMgr (which acts as DF) register the sensing service with it
     */
	private void registerSensorService() {
	    registerAgentService(appIdentifier, null);
    }


	@SuppressWarnings("serial")
    private void setupSensorBehaviours() {
		// The only permanent behaviour we need to add here is the TASKING Command handler
		MessageTemplate taskCommandTemplate = new MessageTemplate(new MatchExpression() {
			@Override
			public boolean match(ACLMessage msg) {
				// match for ExecTask messages
				if (msg.getOntology().equalsIgnoreCase(cmmOntology.getName()) &&
					msg.getLanguage().equalsIgnoreCase(cmmCodec.getName()) &&
					msg.getPerformative() == ACLMessage.REQUEST) {
					
					try {
				        ContentElement ce = getContentManager().extractContent(msg);
				        if (ce instanceof ExecTask) {
				        	return true;
				        }
					}
			        catch (Exception e) {
			        	return false;
			        }
				}
				
				return false;
			}
		});
		
		addBehaviour(new CommandHandlerBehaviour(this, taskCommandTemplate));
    }

	
	private void doConnectProposal() {
	    // retrieve the CtxCoord AID
		// TODO: in the future, handle case of connecting to several CtxCoord agents per appIdentifier
		final AID coordinatorAID = sensorSpecification.getAssignedCoordinatorAddress().getAID();
		PublishAssertions publishContent = new DefaultPublishAssertions();
		
		Map<String, AssertionManager> managedAssertions = sensedAssertionsManager.managedAssertions;
		for (AssertionManager assertionManager : managedAssertions.values()) {
			AssertionDescription assertionDesc = assertionManager.getAssertionDescription();
			String updateMode = assertionManager.getUpdateMode();
			int updateRate = assertionManager.getUpdateRate();
			
			if (assertionDesc != null) {
				AssertionCapability capability = new DefaultAssertionCapability();
				capability.setAssertion(assertionDesc);
				capability.setAvailableUpdateMode(updateMode);
				capability.setAvailableUpdateRate(updateRate);
				
				publishContent.addCapability(capability);
			}
		}
		
		ACLMessage publishMsg = new ACLMessage(ACLMessage.PROPOSE);
		publishMsg.addReceiver(coordinatorAID);
		publishMsg.setLanguage(cmmCodec.getName());
		publishMsg.setOntology(cmmOntology.getName());
		
		try {
	        getContentManager().fillContent(publishMsg, publishContent);
	        addBehaviour(new PublishAssertionsBehaviour(this, publishMsg));
        }
        catch (CodecException e) {
	        e.printStackTrace();
        }
        catch (OntologyException e) {
	        e.printStackTrace();
        }
    }
	
	@Override
	public AgentType getAgentType() {
		return AgentType.CTX_SENSOR;
	}
	
}
