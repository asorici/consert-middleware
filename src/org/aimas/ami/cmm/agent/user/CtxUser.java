package org.aimas.ami.cmm.agent.user;

import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

import java.util.Dictionary;
import java.util.Hashtable;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.RegisterCMMAgentInitiator;
import org.aimas.ami.cmm.agent.SearchCoordinatorInitiator;
import org.aimas.ami.cmm.agent.SearchQueryHandlerInitiator;
import org.aimas.ami.cmm.agent.SearchCoordinatorInitiator.SearchCoordinatorListener;
import org.aimas.ami.cmm.agent.SearchQueryHandlerInitiator.SearchQueryHandlerListener;
import org.aimas.ami.cmm.agent.config.UserSpecification;
import org.aimas.ami.cmm.agent.onto.ContextDomain;
import org.aimas.ami.cmm.agent.onto.DomainDescription;
import org.aimas.ami.cmm.agent.onto.InformDomain;
import org.aimas.ami.cmm.agent.onto.impl.DefaultInformDomain;
import org.aimas.ami.cmm.api.ApplicationUserAdaptor;
import org.aimas.ami.cmm.api.CMMConfigException;
import org.osgi.framework.BundleContext;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;


public class CtxUser extends CMMAgent {
    private static final long serialVersionUID = 8709944221448592230L;
    
    /* Application unique identifier, agent specification */
    private String appIdentifier;
    private UserSpecification userSpecification;
    
    /* Application Adaptor */
    private CtxUserAdaptor applicationAdaptor;
    
    public ApplicationUserAdaptor getApplicationAdaptor() {
    	return applicationAdaptor;
    }
    
    /* CtxUser state */
    private AID queryHandlerAgent;
    private AID coordinatorAgent;
    private boolean actsAsSensor = false;
    
    public void setQueryHandler(AID ctxQueryHandler) {
		this.queryHandlerAgent = ctxQueryHandler;
	}
    
    AID getQueryAgent() {
    	return queryHandlerAgent;
    }
    
    boolean hasQueryHandler() {
    	return queryHandlerAgent != null;
    }
    
	public AID getCoordinatorAgent() {
		return coordinatorAgent;
	}

	public void setCoordinatorAgent(AID coordinatorAgent) {
		this.coordinatorAgent = coordinatorAgent;
	}
	
	public boolean hasCoordinatorAgent() {
		return coordinatorAgent != null;
	}
	
	public void setActsAsSensor(boolean actsAsSensor) {
		this.actsAsSensor = actsAsSensor;
	}
	
    boolean actsAsSensor() {
    	return actsAsSensor;
    }
    
	@Override
    public AgentType getAgentType() {
	    return AgentType.CTX_USER;
    }
	
	
	// SETUP
	//////////////////////////////////////////////////////////////////////////
	@Override
	public void doAgentSpecificSetup(String agentSpecURI, String appIdentifier) {
    	// second one is always the application unique identifier
    	this.appIdentifier = appIdentifier;
    	
    	// ======== STEP 1: configure the agent according to its specification	
    	try {
    		OntModel cmmConfigModel = configurationLoader.loadAgentConfiguration();
    		Resource agentSpecRes = cmmConfigModel.getResource(agentSpecURI);
    		
    		if (agentSpecRes == null) {
    			throw new CMMConfigException("No CtxQueryHandler specification found in configuration model "
    					+ "for URI " + agentSpecURI);
    		}
    		
    		// retrieve specification and access the configured application adaptor service
    		agentSpecification = UserSpecification.fromConfigurationModel(cmmConfigModel, agentSpecRes);
    		userSpecification = (UserSpecification)agentSpecification;
    		assignedOrgMgr = userSpecification.getAssignedManagerAddress().getAID();
    		
    		setupApplicationAdaptor();
    	}
    	catch(CMMConfigException e) {
    		// if we have a local OrgMgr we must signal our initialization failure
    		signalInitializationOutcome(false);
    		return;
    	}
    	
    	// ======== STEP 3:	setup the CtxUser specific permanent behaviors
    	setupUserBehaviours();
    	
    	// after this step initialization of the CtxSensor is complete, so we signal a successful init
    	signalInitializationOutcome(true);
    	
    	
    	// ======== STEP 4: check existence of a CtxQueryHandler to be able to pose queries.
    	// We ask the assigned (or local) OrgMgr for a CtxQueryHandler. If none is found, then
    	// it means we are in dynamic mode, so we will receive our assigned OrgMgr when we enter
    	// a ContextDomain
    	findQueryHandler();
    	findCoordinator();
    	
    	findContextDomain();
	}
	
	@Override
    protected void registerWithAssignedManager() {
		if (assignedOrgMgr != null) {
			addBehaviour(new RegisterCMMAgentInitiator(this));
		}
    }
	

	private void setupApplicationAdaptor() {
	    // Register the CtxUserAdaptor as the class providing a ApplicationUserAdaptor
	    BundleContext context = osgiHelper.getBundleContext();
	    applicationAdaptor = new CtxUserAdaptor(this);
	    
	    Dictionary<String, String> properties = new Hashtable<String, String>();
	    properties.put(ApplicationUserAdaptor.ADAPTOR_NAME, userSpecification.getAgentLocalName());
	    properties.put(ApplicationUserAdaptor.APP_IDENTIFIER_PROPERTY, appIdentifier);
	    
	    context.registerService(ApplicationUserAdaptor.class, applicationAdaptor, properties);
    }
	
	
	private void setupUserBehaviours() {
	    // NOTHING TO SETUP FOR THE MOMENT
    }
	
	private void findQueryHandler() {
	    SearchQueryHandlerListener listener = new SearchQueryHandlerListener() {
			@Override
			public void queryHandlerAgentNotFound(ACLMessage msg) {
				System.out.println(msg);
			}
			
			@Override
			public void queryHandlerAgentFound(AID agentAID) {
				setQueryHandler(agentAID);
			}
		};
		
		addBehaviour(new SearchQueryHandlerInitiator(this, listener));
    }
	
	
	private void findCoordinator() {
	    SearchCoordinatorListener listener = new SearchCoordinatorListener() {
			@Override
			public void coordinatorAgentNotFound(ACLMessage msg) {
				System.out.println(msg);
			}
			
			@Override
			public void coordinatorAgentFound(AID agentAID) {
				coordinatorAgent = agentAID;
				applicationAdaptor.setUserUpdateDestination(agentAID);
			}
		};
		
		addBehaviour(new SearchCoordinatorInitiator(this, listener));
    }
	
	/*
	private void doConnectToQueryHandler() {
		// For now we treat the centralized-local case, where we ask the local OrgMgr
		// what the address of the CtxQueryHandler is
		DFAgentDescription queryHandlerTemplate = new DFAgentDescription();
		ServiceDescription querySD = new ServiceDescription();
		querySD.setType(AgentType.CTX_QUERY_HANDLER.getServiceName());
		querySD.setName(appIdentifier);
		queryHandlerTemplate.addServices(querySD);
		
		SearchConstraints constraints = new SearchConstraints();
		constraints.setMaxResults((long)1);
		
		if (localOrgMgr != null) {
			try {
				DFAgentDescription[] result = DFService.search(this, localOrgMgr, queryHandlerTemplate, constraints);
				if (result != null) {
					setQueryHandler(result[0].getName());
				}
			}
			catch (FIPAException e) {
				// No reason this should actually happen
				e.printStackTrace();
			}
		}
    }
	*/
	
	void findContextDomain() {
	    // We only have to call this if we have a defined OrgMgr.
		// Otherwise, it means we are in a dynamic mode and we will be in fact informed
		// when entering a Context Domain
		AID manager = assignedOrgMgr != null ? assignedOrgMgr : localOrgMgr;
		
		
		ACLMessage informDomainMsg = new ACLMessage(ACLMessage.REQUEST);
		informDomainMsg.setLanguage(CMMAgent.cmmCodec.getName());
		informDomainMsg.setOntology(CMMAgent.cmmOntology.getName());
		informDomainMsg.addReceiver(manager);
		
		InformDomain informDomain = new DefaultInformDomain();
		informDomain.setAppIdentifier(appIdentifier);
		
		try {
            Action informDomainAction = new Action(manager, informDomain);
			getContentManager().fillContent(informDomainMsg, informDomainAction);
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
		
		addBehaviour(new SimpleAchieveREInitiator(this, informDomainMsg) {
            private static final long serialVersionUID = 1L;
			
            @Override
            public void handleInform(ACLMessage msg) {
            	try {
                    DomainDescription domainDesc = (DomainDescription)getContentManager().extractContent(msg);
                    ContextDomain domain = domainDesc.getDomain();
                    
                    applicationAdaptor.setDomainValue(domain.getDomainValue());
                    applicationAdaptor.setContextDimension(domain.getDomainDimension());
            	}
                catch (Exception e) {
                	e.printStackTrace();
                }
            }
		});
		
    }
}
