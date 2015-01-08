package org.aimas.ami.cmm.agent.orgmgr;

import jade.core.AID;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.util.Event;
import jade.wrapper.StaleProxyException;

import java.util.LinkedList;
import java.util.List;

import org.aimas.ami.cmm.agent.CMMAgent;
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
    
    private Event cmmInitEvent;
    private CMMOpEventResult initResult;
    
    private List<ManagedAgentWrapper<?>> initializedAgents;
    
    public CMMInitBehaviour(OrgMgr orgMgr, Event initEvent) {
    	super(orgMgr);
    	
    	this.cmmInitEvent = initEvent;
    	this.initializedAgents = new LinkedList<ManagedAgentWrapper<?>>();
    	
    	// add the sub-behaviours
    	ManagedAgentWrapper<CoordinatorSpecification> managedCoordinator = orgMgr.agentManager.getManagedCoordinator();
    	if (managedCoordinator != null && managedCoordinator.isLocalAgent()) {
    		addSubBehaviour(new AgentInitBehaviour(orgMgr, managedCoordinator, COORD_TIMEOUT));
    	}
    	
    	List<ManagedAgentWrapper<QueryHandlerSpecification>> managedLocalQueryHandlers = orgMgr.agentManager.getManagedQueryHandlers();
    	for (ManagedAgentWrapper<?> managedAgent : managedLocalQueryHandlers) {
    		if (managedAgent.isLocalAgent()) {
    			addSubBehaviour(new AgentInitBehaviour(orgMgr, managedAgent, QUERY_HANDLER_TIMEOUT));
    		}
    	}
    	
    	List<ManagedAgentWrapper<SensorSpecification>> managedLocalSensors = orgMgr.agentManager.getManagedSensors();
    	for (ManagedAgentWrapper<?> managedAgent : managedLocalSensors) {
    		if (managedAgent.isLocalAgent()) {
    			addSubBehaviour(new AgentInitBehaviour(orgMgr, managedAgent, SENSOR_TIMEOUT));
    		}
    	}
    	
    	List<ManagedAgentWrapper<UserSpecification>> managedLocalUsers = orgMgr.agentManager.getManagedUsers();
    	for (ManagedAgentWrapper<?> managedAgent : managedLocalUsers) {
    		if (managedAgent.isLocalAgent()) {
    			addSubBehaviour(new AgentInitBehaviour(orgMgr, managedAgent, USER_TIMEOUT));
    		}
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
    			
    			initResult = new CMMOpEventResult(initError, faultyAgent);
    		}
    	}
    	
    	return shouldFinish;
    }
    
    @Override
    public int onEnd() {
    	if (initResult == null) {
    		// yey, we had no errors during the initialization chain
    		System.out.println("CMM INIT SUCCESSFUL!");
    		
    		initResult = new CMMOpEventResult();
    	}
    	else {
    		if (initResult.hasResult()) {
	    		// We know to interpret a value for the initResult as an indication of the agent that failed.
    			// For the external reply however, we need to wrap this as an exception.
    			System.out.println("CMM INIT FAILED. No initialization confirmation received from " 
	    			+ ((ManagedAgentWrapper<?>)initResult.getResult()).getAgentSpecification().getAgentName());
    			
    			initResult = new CMMOpEventResult(new Exception("CMM INIT FAILED. "
    					+ "No initialization confirmation received from " 
    	    			+ ((ManagedAgentWrapper<?>)initResult.getResult()).getAgentSpecification().getAgentName()));
    		}
	    	else {
	    		System.out.println("CMM INIT FAILED. Exception during initialization: " + initResult.getError());
	    	}
    		
    		// we had an error so kill all the initialized agents because it's all or nothing
    		for (int index = initializedAgents.size() - 1; index >= 0; index--) {
    			ManagedAgentWrapper<?> agentWrapper = initializedAgents.get(index);
    			try {
	                agentWrapper.getAgentController().kill();
                }
                catch (StaleProxyException e) {
                	// we ignore the exception since it means the agent is already dead and there's nothing more we can do
                }
    		}
    	}
    	
    	// we notify here the result of the initialization
    	cmmInitEvent.notifyProcessed(initResult);
    	
    	return 0;
    }
    
    
    private class AgentInitBehaviour extends SimpleBehaviour {
        private static final long serialVersionUID = 2701478816359859719L;
		
        private static final int REQUEST_START = 0;
        private static final int AWAIT_CONFIRM = 1;
        
        private int state = REQUEST_START;
        private boolean finished = false;
    	private int exitCode = INIT_SUCCESS;
    	private long endingTime = 0;
    	
        private OrgMgr orgMgr;
    	private ManagedAgentWrapper<?> cmmAgentWrapper;
    	private long timeoutThreshold;
    	
    	private MessageTemplate initConfirmationTemplate;
    	private Exception initError;
    	
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
    	
    	
		@SuppressWarnings("serial")
        @Override
        public void action() {
			switch(state) {
				case REQUEST_START: {
					if (!cmmAgentWrapper.isActive()) {
						// 1a) start the agent
						try {
							System.out.println("Starting agent: " + cmmAgentWrapper.agentSpecification.getAgentLocalName());
							cmmAgentWrapper.start();
							state = AWAIT_CONFIRM;
						}
		                catch (StaleProxyException e) {
			                // we have failed to initialize miserably
		                	exitCode = INIT_FAILURE;
		                	finished = true;
		                	
		                	initError = e;
		                	break;
		                }
						
						// 1b) create confirmation message template
						final AID cmmAgentAID = cmmAgentWrapper.getAgentSpecification().getAgentAddress().getAID();
						initConfirmationTemplate = new MessageTemplate(new MatchExpression() {
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
						
						// 1c) set the endingTime if there is a timeoutThreshold
						if (timeoutThreshold != 0) {
							endingTime = CMMAgent.currentTimeMillis() + timeoutThreshold;
						}
					}
					else {
						// agent is already active, so we don't need to do anything
						exitCode = INIT_SUCCESS;
						finished = true;
					}
					
					break;
				}
				
				case AWAIT_CONFIRM: {
					// 2) attempt to receive a initConfirm message within given time threshold
					ACLMessage confirmMessage = orgMgr.receive(initConfirmationTemplate);
					if (confirmMessage != null) {
						boolean initialized = initSuccessful(confirmMessage);
						if (initialized) {
							exitCode = INIT_SUCCESS;
							initializedAgents.add(cmmAgentWrapper);
						}
						else {
							exitCode = INIT_FAILURE;
						}
						finished = true;
					}
					else {
						if (timeoutThreshold > 0) {
							long blockTime = endingTime - CMMAgent.currentTimeMillis();
							  
						    if(blockTime <= 0) {
						    	// timeout Expired - we can end here and report a failure
						    	exitCode = INIT_FAILURE;
						    	finished = true;
						    }
						    else { 
						    	//timeout not yet expired.
						    	block(blockTime);
						    }
						}
						else {
							block();
						}
						
						break;
					}
				}
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
