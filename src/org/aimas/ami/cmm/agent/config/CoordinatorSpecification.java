package org.aimas.ami.cmm.agent.config;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

public class CoordinatorSpecification extends AgentSpecification {
	private AgentAddress managerAddress;
	
	public CoordinatorSpecification(AgentAddress agentAddress, AgentPolicy controlPolicy, 
			AgentAddress managerAddress) {
	    
		super(agentAddress, AgentType.CTX_COORD, controlPolicy);
		this.managerAddress = managerAddress;
    }
	
	
	public AgentAddress getManagerAddress() {
		return managerAddress;
	}
	
	
	public static CoordinatorSpecification fromConfigurationModel(OntModel cmmConfigModel, Resource coordSpec) {
		AgentAddress agentAddress = AgentSpecification.getAddressFromConfig(cmmConfigModel, coordSpec);
		AgentPolicy controlPolicy = AgentSpecification.getPolicyFromConfig(cmmConfigModel, coordSpec);
		
		AgentAddress managerAddress = AgentAddress.fromConfigurationModel(cmmConfigModel, 
			coordSpec.getPropertyResourceValue(OrgConf.assignedOrgManager));	
		
		return new CoordinatorSpecification(agentAddress, controlPolicy, managerAddress);
	}
}
