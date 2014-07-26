package org.aimas.ami.cmm.agent.config;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

public class QueryHandlerSpecification extends AgentSpecification {
	private boolean isPrimary;
	private AgentAddress coordinatorAddress;
	private String queryAdaptorClass;
	
	public QueryHandlerSpecification(AgentAddress agentAddress, AgentPolicy controlPolicy, 
			boolean isPrimary, String queryAdaptorClass, AgentAddress coordinatorAddress) {
		super(agentAddress, AgentType.CTX_QUERY_HANDLER, controlPolicy);
		
		this.isPrimary = isPrimary;
		this.queryAdaptorClass = queryAdaptorClass;
		this.coordinatorAddress = coordinatorAddress;
    }

	public boolean isPrimary() {
		return isPrimary;
	}
	
	public String getQueryAdaptorClass() {
	    return queryAdaptorClass;
    }
	
	public AgentAddress getAssignedCoordinatorAddress() {
		return coordinatorAddress;
	}
	
	
	public static QueryHandlerSpecification fromConfigurationModel(OntModel cmmConfigModel, Resource queryHandlerSpec) {
		AgentAddress agentAddress = AgentSpecification.getAddressFromConfig(cmmConfigModel, queryHandlerSpec);
		boolean isPrimary = queryHandlerSpec.getProperty(OrgConf.isPrimaryQueryHandler).getBoolean();
		
		String queryAdaptorClass = getAdaptorClass(cmmConfigModel, queryHandlerSpec.getPropertyResourceValue(OrgConf.hasQueryAdaptor));
		
		AgentAddress coordinatorAddress = AgentAddress.fromConfigurationModel(cmmConfigModel, 
			queryHandlerSpec.getPropertyResourceValue(OrgConf.assignedCoordinator));	
		
		// We don't have any CtxQueryHandler specific control policies yet
		return new QueryHandlerSpecification(agentAddress, null, isPrimary, queryAdaptorClass, coordinatorAddress);
	}
	
	private static String getAdaptorClass(OntModel cmmConfigModel, Resource assertionAdaptorRes) {
    	return assertionAdaptorRes.getProperty(OrgConf.hasQualifiedName).getString();
    }
}
