package org.aimas.ami.cmm.agent.config;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

public class QueryHandlerSpecification extends AgentSpecification {
	private boolean isPrimary;
	private AgentAddress assignedOrgMgrAddress;
	
	public QueryHandlerSpecification(AgentAddress agentAddress, AgentPolicy controlPolicy, 
			boolean isPrimary, AgentAddress assignedOrgMgrAddress) {
		super(agentAddress, AgentType.CTX_QUERY_HANDLER, controlPolicy);
		
		this.isPrimary = isPrimary;
		this.assignedOrgMgrAddress = assignedOrgMgrAddress;
    }

	public boolean isPrimary() {
		return isPrimary;
	}
	
	public AgentAddress getAssignedManagerAddress() {
		return assignedOrgMgrAddress;
	}
	
	
	public static QueryHandlerSpecification fromConfigurationModel(OntModel cmmConfigModel, Resource queryHandlerSpec) {
		AgentAddress agentAddress = AgentSpecification.getAddressFromConfig(cmmConfigModel, queryHandlerSpec);
		boolean isPrimary = queryHandlerSpec.getProperty(OrgConf.isPrimaryQueryHandler).getBoolean();
		
		AgentAddress assignedOrgMgrAddress = AgentAddress.fromConfigurationModel(cmmConfigModel, 
			queryHandlerSpec.getPropertyResourceValue(OrgConf.assignedOrgManager));	
		
		// We don't have any CtxQueryHandler specific control policies yet
		return new QueryHandlerSpecification(agentAddress, null, isPrimary, assignedOrgMgrAddress);
	}
}
