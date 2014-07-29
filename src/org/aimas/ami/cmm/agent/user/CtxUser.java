package org.aimas.ami.cmm.agent.user;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

import java.util.Dictionary;
import java.util.Hashtable;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.config.UserSpecification;
import org.aimas.ami.cmm.agent.onto.ContextDomain;
import org.aimas.ami.cmm.agent.onto.DomainDescription;
import org.aimas.ami.cmm.agent.onto.InformDomain;
import org.aimas.ami.cmm.agent.onto.impl.DefaultInformDomain;
import org.aimas.ami.cmm.exceptions.CMMConfigException;
import org.aimas.ami.cmm.resources.ApplicationUserAdaptor;
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
    	
    	// ======== STEP 2: configure the agent according to its specification	
    	try {
	    	// configure access to resource
    		doResourceAccessConfiguration();
    		
    		OntModel cmmConfigModel = configurationLoader.loadAppConfiguration();
    		Resource agentSpecRes = cmmConfigModel.getResource(agentSpecURI);
    		
    		if (agentSpecRes == null) {
    			throw new CMMConfigException("No CtxQueryHandler specification found in configuration model "
    					+ "for URI " + agentSpecURI);
    		}
    		
    		// retrieve specification and access the configured application adaptor service
    		agentSpecification = UserSpecification.fromConfigurationModel(cmmConfigModel, agentSpecRes);
    		userSpecification = (UserSpecification)agentSpecification;
    		
    		setupApplicationAdaptor();
    	}
    	catch(CMMConfigException e) {
    		// if we have a local OrgMgr we must signal our initialization failure
    		signalInitializationOutcome(false);
    		return;
    	}
    	
    	// ======== STEP 3:	setup the CtxUser specific permanent behaviours
    	setupUserBehaviours();
    	
    	// after this step initialization of the CtxSensor is complete, so we signal a successful init
		signalInitializationOutcome(true);
    	
    	// ======== STEP 4: check existence of a CtxQueryHandler to be able to pose queries.
    	// This check involves the following steps:
    	//   - if we are in a centralized app setting (what we are doing in this initial version):
    	//     ask the local OrgMgr for the address of the CtxQueryHandler
    	//   - if we are in the decentralized app setting, we either have a remote OrgMgr specification, or
    	//     we operate in the dynamic mode which means we will receive the remote OrgMgr address
    	//     from our local OrgMgr. We then repeat the same thing as above with the remote OrgMgr
    	doConnectToQueryHandler();
    	
    	findContextDomain();
	}
	

	private void setupApplicationAdaptor() {
	    // Register the CtxUserAdaptor as the class providing a ApplicationUserAdaptor
	    BundleContext context = osgiHelper.getBundleContext();
	    applicationAdaptor = new CtxUserAdaptor(this);
	    
	    Dictionary<String, String> properties = new Hashtable<String, String>();
	    properties.put(ApplicationUserAdaptor.APP_IDENTIFIER_PROPERTY, appIdentifier);
	    
	    context.registerService(ApplicationUserAdaptor.class, applicationAdaptor, properties);
    }
	
	
	private void setupUserBehaviours() {
	    // NOTHING TO SETUP FOR THE MOMENT
    }
	
	
	private void doConnectToQueryHandler() {
		// For now we treet the centralized-local case case, where we ask the local OrgMgr
		// what the address of the CtxQueryHandler is
		DFAgentDescription queryHandlerTemplate = new DFAgentDescription();
		ServiceDescription querySD = new ServiceDescription();
		querySD.setType(AgentType.CTX_QUERY_HANDLER.getServiceType());
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
	
	
	void findContextDomain() {
	    // We only have to call this if we have a defined OrgMgr.
		// Otherwise, it means we are in a dynamic mode and we will be in fact informed
		// when entering a Context Domain
		if (localOrgMgr != null) {
			ACLMessage informDomainMsg = new ACLMessage(ACLMessage.REQUEST);
			informDomainMsg.setLanguage(getCMMCodec().getName());
			informDomainMsg.setOntology(getCMMOntology().getName());
			informDomainMsg.addReceiver(localOrgMgr);
			
			InformDomain content = new DefaultInformDomain();
			content.setAppIdentifier(appIdentifier);
			try {
	            getContentManager().fillContent(informDomainMsg, content);
            }
            catch (Exception e) {}
			
			addBehaviour(new SimpleAchieveREInitiator(this, informDomainMsg) {
                private static final long serialVersionUID = 1L;
				
                @Override
                public void handleInform(ACLMessage msg) {
                	try {
	                    DomainDescription domainDesc = (DomainDescription)getContentManager().extractContent(msg);
	                    ContextDomain domain = domainDesc.getDomain();
	                    
	                    applicationAdaptor.setDomainValue(domain.getDomainValue());
                	}
                    catch (Exception e) {}
                }
			});
		}
    }
}
