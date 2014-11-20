package org.aimas.ami.cmm.agent.sensor;

import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;

import java.util.List;
import java.util.Map;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.RegisterCMMAgentInitiator;
import org.aimas.ami.cmm.agent.SearchCoordinatorInitiator;
import org.aimas.ami.cmm.agent.SearchCoordinatorInitiator.SearchCoordinatorListener;
import org.aimas.ami.cmm.agent.config.SensingPolicy;
import org.aimas.ami.cmm.agent.config.SensorSpecification;
import org.aimas.ami.cmm.agent.onto.AssertionCapability;
import org.aimas.ami.cmm.agent.onto.AssertionDescription;
import org.aimas.ami.cmm.agent.onto.ExecTask;
import org.aimas.ami.cmm.agent.onto.PublishAssertions;
import org.aimas.ami.cmm.agent.onto.impl.DefaultAssertionCapability;
import org.aimas.ami.cmm.agent.onto.impl.DefaultPublishAssertions;
import org.aimas.ami.cmm.agent.sensor.SensingManager.AssertionActiveListener;
import org.aimas.ami.cmm.api.CMMConfigException;
import org.aimas.ami.cmm.vocabulary.CoordConf;
import org.aimas.ami.cmm.vocabulary.SensorConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class CtxSensor extends CMMAgent implements AssertionActiveListener {
    private static final long serialVersionUID = 4254689617972269011L;
    
    public static enum SensorState {
    	INACTIVE, ACTIVE, CONNECTED, TRANSMITTING
    }
    
    /* Application unique identifier and agent specification */
    private String appIdentifier;
    private SensorSpecification sensorSpecification;
    
    /* SensingManager, CtxSensor state and the CtxCoord currently connected to */
    private SensingManager sensedAssertionsManager;
    private SensorState sensorState = SensorState.INACTIVE;
    private AID currentCoordinator;
    
    @Override
	public AgentType getAgentType() {
		return AgentType.CTX_SENSOR;
	}
    
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
    
    AID getCurrentCoordinator() {
    	return currentCoordinator;
    }
    
    @Override
    public void doAgentSpecificSetup(String agentSpecURI, String appIdentifier) {
    	// ======== STEP 1: configure the agent according to its specification	
    	try {
    		OntModel cmmConfigModel = configurationLoader.loadAgentConfiguration();
    		Resource agentSpecRes = cmmConfigModel.getResource(agentSpecURI);
    		
    		if (agentSpecRes == null) {
    			throw new CMMConfigException("No CtxSensor specification found in configuration model " + "for URI " + agentSpecURI);
    		}
    		
    		// retrieve specification and create sensed assertions manager
    		agentSpecification = SensorSpecification.fromConfigurationModel(cmmConfigModel, agentSpecRes);
    		sensorSpecification = (SensorSpecification)agentSpecification;
    		assignedOrgMgr = sensorSpecification.getAssignedManagerAddress().getAID();
    		
    		configureSensingManager(sensorSpecification.getSensingPolicies());
    		
    		// after this step initialization of the CtxSensor is complete, so we signal a successful init
    		signalInitializationOutcome(true);
    		
    		// last thing we do in this step is switch our state to ACTIVE
    		sensorState = SensorState.ACTIVE; 
    	}
    	catch(CMMConfigException e) {
    		// if we have a local OrgMgr we must signal our initialization failure
    		signalInitializationOutcome(false);
    		return;
    	}
    	
    	// ======== STEP 2: if there is a local OrgMgr register with it as part of the DF
    	// A CMM agent will specify two attributes in its service description: app-name
    	// and agent-type.
    	registerSensorService();
    	
    	// ======== STEP 3:	setup the CtxSensor specific permanent behaviors
    	setupSensorBehaviours();
    	
    	// ======== STEP 4: Find the CtxCoord by asking the assigned (or local) OrgMgr and then 
    	// connect to the CtxCoord by publishing the proposal to supply updates of the sensed ContextAssertions.
    	// If we do not find a coordinator, it means we are in dynamic mode, and we will receive an assignedOrgMgr
    	findCoordinator();
    }
    
    
    private void configureSensingManager(List<SensingPolicy> sensingPolicies) throws CMMConfigException {
    	sensedAssertionsManager = new SensingManager(this, this);
    	
    	for (SensingPolicy sensingPolicy : sensingPolicies) {
	    	Resource assertionRes = sensingPolicy.getContextAssertionRes();
	    	String adaptorClassName = sensingPolicy.getAssertionAdaptorClass();
	    	String configureDoc = sensingPolicy.getFileNameOrURI();
	    	
	    	// Retrieve the ContextAssertion-specific sensing policy, the one that specifies
	    	// update mode, update rate and the list of physical sensor IDs to which it applies
	    	OntModel sensingConfigModel = getConfigurationLoader().load(configureDoc);
	    	Resource assertionSenseSpec = 
	    		sensingConfigModel.listResourcesWithProperty(CoordConf.forContextAssertion, assertionRes).next();
	    	
	    	// Get the updateMode and updateRate assertion update specifications
	    	String updateMode = assertionSenseSpec.getPropertyResourceValue(SensorConf.hasUpdateMode).getLocalName();
	    	int updateRate = 0;
	    	
	    	Statement updateRateStmt = assertionSenseSpec.getProperty(SensorConf.hasUpdateRate);
	    	if (updateRateStmt != null) {
	    		updateRate = updateRateStmt.getInt();
	    	}
	    	
	    	// When we create the assertion manager updates are not yet enabled.
	    	// Enabling will be done by the CtxSensor agent, once it gets the OK from the CtxCoord
	    	// for the ContextAssertions it wants to publish.
	    	sensedAssertionsManager.addManagedContextAssertion(assertionRes.getURI(), adaptorClassName, updateMode, updateRate);
	    }
    }
    
    @Override
    public void assertionActiveChanged(String assertionResourceURI, boolean active) {
    	if (active) {
	    	// It means that one of the managed assertions has resumed updates, so we set the
	    	// CtxSensor in a TRANSMITTING state if we are in the CONNECTED one
	    	if (sensorState == SensorState.CONNECTED) {
	    		sensorState = SensorState.TRANSMITTING;
	    	}
	    }
	    else {
	    	// If we are in the transmitting state and one of our assertions stops its updates,
	    	// then we need to figure out if others we are managing are still active. If no more
	    	// are active, set the state to just CONNECTED
	    	if (sensorState == SensorState.TRANSMITTING) {
	    		boolean hasActive = false;
	    		
	    		for (String assertionURI : sensedAssertionsManager.getManagedAssertions().keySet()) {
	    			if (!assertionURI.equals(assertionResourceURI)) {
	    				AssertionManager assertionManager = sensedAssertionsManager.getManagedAssertions().get(assertionURI);
	    				if (assertionManager.isActive() && assertionManager.updateEnabled()) {
	    					hasActive = true;
	    					break;
	    				}
	    			}
	    		}
	    		
	    		if (!hasActive) {
	    			sensorState = SensorState.CONNECTED;
	    		}
	    	}
	    }
    }
    
    /** If there is a local OrgMgr (which acts as DF) register the sensing service with it */
	private void registerSensorService() {
	    registerAgentService(appIdentifier, null);
    }
	
	
	@Override
    protected void registerWithAssignedManager() {
		if (assignedOrgMgr != null) {
			addBehaviour(new RegisterCMMAgentInitiator(this));
		}
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
				        Action contentAction = (Action)getContentManager().extractContent(msg);
				        if (contentAction.getAction() instanceof ExecTask) {
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
		
		addBehaviour(new TaskingCommandBehaviour(this, taskCommandTemplate));
    }
	
	
	private void findCoordinator() {
	    SearchCoordinatorListener listener = new SearchCoordinatorListener() {
			@Override
			public void coordinatorAgentNotFound(ACLMessage msg) {
				System.out.println(msg);
			}
			
			@Override
			public void coordinatorAgentFound(AID agentAID) {
				currentCoordinator = agentAID;
				sensedAssertionsManager.setAssertionUpdateDestination(agentAID);
				
				doConnectProposal(agentAID);
			}
		};
		
		addBehaviour(new SearchCoordinatorInitiator(this, listener));
    }

	
	private void doConnectProposal(final AID coordinatorAID) {
		PublishAssertions publishContent = new DefaultPublishAssertions();
		
		Map<String, AssertionManager> managedAssertions = sensedAssertionsManager.getManagedAssertions();
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
			Action publishAssertionsAction = new Action(getAID(), publishContent);
			
	        getContentManager().fillContent(publishMsg, publishAssertionsAction);
	        addBehaviour(new PublishAssertionsBehaviour(this, sensedAssertionsManager, publishMsg));
        }
        catch (CodecException e) {
	        e.printStackTrace();
        }
        catch (OntologyException e) {
	        e.printStackTrace();
        }
    }
}
