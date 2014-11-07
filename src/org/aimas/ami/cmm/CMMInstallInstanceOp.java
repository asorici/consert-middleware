package org.aimas.ami.cmm;

import jade.util.Event;
import jade.wrapper.AgentController;

import org.aimas.ami.cmm.agent.config.AgentAddress;
import org.aimas.ami.cmm.agent.config.ManagerSpecification;
import org.aimas.ami.cmm.agent.config.ManagerSpecification.ManagerType;
import org.aimas.ami.cmm.agent.orgmgr.CMMEventResult;
import org.aimas.ami.cmm.agent.orgmgr.CMMPlatformInterface;
import org.aimas.ami.cmm.agent.orgmgr.OrgMgr;
import org.aimas.ami.cmm.api.CMMConfigException;
import org.aimas.ami.cmm.utils.AgentConfigLoader;
import org.aimas.ami.contextrep.utils.BundleResourceManager;
import org.osgi.framework.Bundle;

import com.hp.hpl.jena.ontology.OntModel;

public class CMMInstallInstanceOp extends CMMInstanceStateOp {
	
	public CMMInstallInstanceOp(ContextDomainWrapper contextDomainInfo, CMMStateOperationType operationType, CMMPlatformManager cmmPlatformManager) {
		super(contextDomainInfo, operationType, cmmPlatformManager);
	}
	
	@Override
	public Void call() throws Exception {
		// STEP 1: get the CMM Instance Bundle and start it if needed 
		String applicationId = contextDomainInfo.getApplicationId();
		String contextDimensionURI = contextDomainInfo.getContextDimensionURI();
		String contextDomainValueURI = contextDomainInfo.getContextDomainValueURI();
		
		Bundle cmmInstanceResourceBundle = cmmPlatformManager.getCMMInstanceResourceBundle(applicationId, contextDimensionURI, contextDomainValueURI);
		
		if (cmmInstanceResourceBundle == null) {
			throw new CMMConfigException("No instance resource bundle found for the following CMM instance identifier information: "
					+ "applicationId = " + applicationId + ", "
					+ "contextDimensionURI = " + contextDimensionURI + ", "
					+ "contextDomainValueURI = " + contextDomainValueURI);
		}
		
		if (cmmInstanceResourceBundle.getState() != Bundle.ACTIVE) {
			// This operation has no significant effect, if not for the launching of a TimeService
			cmmInstanceResourceBundle.start();
		}
		
		// STEP 2: instantiate a CMM instance resource manager and an AgentActivator
		BundleResourceManager cmmInstanceResourceManager = new BundleResourceManager(cmmInstanceResourceBundle);
		AgentConfigLoader agentConfigLoader = new AgentConfigLoader(cmmInstanceResourceManager);
		OntModel agentConfigModel = agentConfigLoader.loadAgentConfiguration();
		
		// STEP 3: get the instance-specific OrgMgr specification
		// If there is no orgMgrSpec, it means we are only deploying agents that are configured to 
		// connect to a remote OrgMgr. Therefore, we will create a default ManagerSpecification for a Central OrgMgr
		// whose sole role is to instantiate the agents and manage any deployment specification modifications
		ManagerSpecification orgMgrSpec = ManagerSpecification.fromConfigurationModel(agentConfigModel);
		if (orgMgrSpec == null) {
			String orgMgrName = "OrgMgr" + "__" + applicationId + "__" + contextDimensionURI + "__" + contextDomainValueURI;
			AgentAddress orgMgrAddress = new AgentAddress(orgMgrName, cmmPlatformManager.getPlatformSpecification().getPlatformAgentContainer());
			orgMgrSpec = new ManagerSpecification(orgMgrAddress, ManagerType.Central, null);
		}
		
		// STEP 4: create the OrgMgr and start him
		String orgMgrName = orgMgrSpec.getAgentAddress().getLocalName();
		
		// send the following elements as parameters of the OrgMgr: CMM Instance Resource Bundle location, OrgMgr type
		Object[] orgMgrParams = new Object[] { cmmInstanceResourceBundle.getLocation(), orgMgrSpec.getManagerType() };
		
		// We assume that nothing can go wrong in the OrgMgr creation step
		AgentController orgMgrController = cmmPlatformManager.createNewAgent(orgMgrName, OrgMgr.class.getName(), orgMgrParams);
		orgMgrController.start();
		
		// STEP 5: use the CMMPlatformInterface to request that the CMM Instance be created
		CMMPlatformInterface platformInterface = orgMgrController.getO2AInterface(CMMPlatformInterface.class);
		Event initRequestEvent = platformInterface.createCMMInstance();
		
		CMMEventResult cmmInitResult = (CMMEventResult)initRequestEvent.waitUntilProcessed();
		if (cmmInitResult.hasError()) {
			throw cmmInitResult.getError();
		}
		
		return null;
	}
	
}
