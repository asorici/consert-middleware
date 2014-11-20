package org.aimas.ami.cmm.agent.config;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

public class CoordinatorSpecification extends AgentSpecification {
	private AgentAddress assignedOrgMgrAddress;
	
	public CoordinatorSpecification(AgentAddress agentAddress, AgentPolicy controlPolicy, 
			AgentAddress managerAddress) {
	    
		super(agentAddress, AgentType.CTX_COORD, controlPolicy);
		this.assignedOrgMgrAddress = managerAddress;
    }
	
	
	public AgentAddress getAssignedManagerAddress() {
		return assignedOrgMgrAddress;
	}
	
	
	public static CoordinatorSpecification fromConfigurationModel(OntModel cmmConfigModel, Resource coordSpec) {
		AgentAddress agentAddress = AgentSpecification.getAddressFromConfig(cmmConfigModel, coordSpec);
		AgentPolicy controlPolicy = AgentSpecification.getPolicyFromConfig(cmmConfigModel, coordSpec);
		
		AgentAddress managerAddress = AgentAddress.fromConfigurationModel(cmmConfigModel, 
			coordSpec.getPropertyResourceValue(OrgConf.assignedOrgManager));	
		
		return new CoordinatorSpecification(agentAddress, controlPolicy, managerAddress);
	}
}
