package org.aimas.ami.cmm.agent.orgmgr;

import jade.core.AID;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.wrapper.StaleProxyException;

import java.util.List;

import org.aimas.ami.cmm.agent.config.CoordinatorSpecification;
import org.aimas.ami.cmm.agent.config.QueryHandlerSpecification;
import org.aimas.ami.cmm.agent.config.SensorSpecification;
import org.aimas.ami.cmm.agent.config.UserSpecification;

public class CMMInitBehaviour extends SequentialBehaviour {
    private static final long serialVersionUID = -1800440310987500098L;
    
    static final int INIT_SUCCESS = 	0;
    static final int INIT_FAILURE =    -1;
    
    static final long COORD_TIMEOUT = 60000;
    static final long QUERY_HANDLER_TIMEOUT = 10000;
    static final long SENSOR_TIMEOUT = 30000;
    static final long USER_TIMEOUT = 10000;
    
    private CMMInitListener initListener;
    private CMMInitResult initResult;
    
    public CMMInitBehaviour(OrgMgr orgMgr, CMMInitListener initListener) {
    	super(orgMgr);
    	
    	this.initListener = initListener;
    	
    	// add the sub-behaviours
    	List<ManagedAgentWrapper<CoordinatorSpecification>> managedCoordinators = orgMgr.agentManager.getManagedCoordinators();
    	for (ManagedAgentWrapper<CoordinatorSpecification> managedAgent : managedCoordinators) {
    		addSubBehaviour(new AgentInitBehaviour(orgMgr, managedAgent, COORD_TIMEOUT));
    	}
    	
    	List<ManagedAgentWrapper<QueryHandlerSpecification>> managedQueryHandlers = orgMgr.agentManager.getManagedQueryHandlers();
    	for (ManagedAgentWrapper<QueryHandlerSpecification> managedAgent : managedQueryHandlers) {
    		addSubBehaviour(new AgentInitBehaviour(orgMgr, managedAgent, QUERY_HANDLER_TIMEOUT));
    	}
    	
    	List<ManagedAgentWrapper<SensorSpecification>> managedSensors = orgMgr.agentManager.getManagedSensors();
    	for (ManagedAgentWrapper<SensorSpecification> managedAgent : managedSensors) {
    		addSubBehaviour(new AgentInitBehaviour(orgMgr, managedAgent, SENSOR_TIMEOUT));
    	}
    	
    	List<ManagedAgentWrapper<UserSpecification>> managedUsers = orgMgr.agentManager.getManagedUsers();
    	for (ManagedAgentWrapper<UserSpecification> managedAgent : managedUsers) {
    		addSubBehaviour(new AgentInitBehaviour(orgMgr, managedAgent, USER_TIMEOUT));
    	}
    }
    
    @Override
    protected boolean checkTermination(boolean currentDone, int currentResult) {
    	boolean shouldFinish = super.checkTermination(currentDone, currentResult);
    	
    	if (!shouldFinish) {
    		if (currentDone && currentResult == INIT_FAILURE) {
    			// we can stop in advance since one of our agents has failed to confirm initialization
    			shouldFinish = true;
    			
    			// at the same time we set our error object
    			AgentInitBehaviour currentInitBehaviour = (AgentInitBehaviour)getCurrent();
    			ManagedAgentWrapper<?> faultyAgent = currentInitBehaviour.getCMMAgent();
    			Exception initError = currentInitBehaviour.getInitError();
    			
    			initResult = new CMMInitResult(initError, faultyAgent);
    		}
    	}
    	
    	return shouldFinish;
    }
    
    @Override
    public int onEnd() {
    	if (initResult == null) {
    		// yey, we had no errors during the initialization chain
    		initResult = new CMMInitResult();
    	}
    	
    	initListener.notifyInitResult(initResult);
    	
    	return 0;
    }
    
    
    private static class AgentInitBehaviour extends SimpleBehaviour {
        private static final long serialVersionUID = 2701478816359859719L;
		
        private OrgMgr orgMgr;
    	private ManagedAgentWrapper<?> cmmAgentWrapper;
    	private long timeoutThreshold;
    	
    	private Exception initError;
    	
    	private boolean finished = false;
    	private int exitCode = INIT_SUCCESS;
    	
    	public AgentInitBehaviour(OrgMgr orgMgr, ManagedAgentWrapper<?> cmmAgentWrapper, long timeoutThreshold) {
    		super(orgMgr);
    		this.orgMgr = orgMgr;
    		this.cmmAgentWrapper = cmmAgentWrapper;
    		this.timeoutThreshold = timeoutThreshold;
    	}
    	
    	public ManagedAgentWrapper<?> getCMMAgent() {
    		return cmmAgentWrapper;
    	}
    	
    	public Exception getInitError() {
    		return initError;
    	}
    	
    	
		@Override
        public void action() {
			if (!cmmAgentWrapper.isActive()) {
				// 1) start the agent
				try {
	                cmmAgentWrapper.start();
				}
                catch (StaleProxyException e) {
	                // we have failed to initialize miserably
                	exitCode = INIT_FAILURE;
                	finished = true;
                	
                	initError = e;
                }
				
				// 2) wait for the confirmation message which is an INFORM from the corresponding 
				// CMM Agent of the form initConfirm:true or initConfirm:false
				final AID cmmAgentAID = cmmAgentWrapper.getAgentSpecification().getAgentAddress().getAID();
				MessageTemplate initConfirmationTemplate = new MessageTemplate(new MatchExpression() {
					@Override
					public boolean match(ACLMessage msg) {
						if (msg.getPerformative() != ACLMessage.INFORM) {
							return false;
						}
						
						if (!msg.getSender().equals(cmmAgentAID)) {
							return false;
						}
						
						String[] msgComponents = msg.getContent().split(":");
						if (!msgComponents[0].equals("initConfirm") || msgComponents.length != 2) {
							return false;
						}
						
						return true;
					}
				});
				
				// attempt to receive a initConfirm message for the given time threshold
				ACLMessage confirmMessage = orgMgr.blockingReceive(initConfirmationTemplate, timeoutThreshold);
				if (confirmMessage == null) {
					exitCode = INIT_SUCCESS;
				}
				else {
					boolean initialized = initSuccessful(confirmMessage);
					exitCode = initialized ? INIT_SUCCESS : INIT_FAILURE;
				}
				
				finished = true;
			}
			else {
				exitCode = INIT_SUCCESS;
				finished = true;
			}
        }
		
		
		private boolean initSuccessful(ACLMessage confirmMessage) {
			String[] msgComponents = confirmMessage.getContent().split(":");
			return Boolean.parseBoolean(msgComponents[1]);
		}
		
		@Override
        public boolean done() {
	        return finished;
        }
		
		@Override
		public int onEnd() {
			return exitCode;
		}
    }
}
