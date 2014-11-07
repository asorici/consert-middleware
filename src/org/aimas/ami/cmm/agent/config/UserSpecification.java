package org.aimas.ami.cmm.agent.config;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

public class UserSpecification extends AgentSpecification {
	private AgentAddress assignedManagerAddress;
	
	public UserSpecification(AgentAddress agentAddress, AgentPolicy controlPolicy, AgentAddress assignedManagerAddress) {
	    super(agentAddress, AgentType.CTX_USER, controlPolicy);
	}
	
	public AgentAddress getAssignedManagerAddress() {
		return assignedManagerAddress;
	}
	
	public boolean hasAssignedManagerAddress() {
		return assignedManagerAddress != null;
	}
	
	public static UserSpecification fromConfigurationModel(OntModel cmmConfigModel, Resource userSpec) {
		AgentAddress agentAddress = AgentSpecification.getAddressFromConfig(cmmConfigModel, userSpec);
		
		AgentAddress assignedManagerAddress = AgentAddress.fromConfigurationModel(cmmConfigModel, 
				userSpec.getPropertyResourceValue(OrgConf.assignedOrgManager));
		
		// We don't have any Control Policy for the CtxUser for now
		return new UserSpecification(agentAddress, null, assignedManagerAddress);
	}
}
