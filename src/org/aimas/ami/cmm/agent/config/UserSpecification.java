package org.aimas.ami.cmm.agent.config;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

public class UserSpecification extends AgentSpecification {
	private String applicationAdaptorClass;
	
	public UserSpecification(AgentAddress agentAddress, AgentPolicy controlPolicy, String applicationAdaptorClass) {
	    super(agentAddress, AgentType.CTX_USER, controlPolicy);
	    
	    this.applicationAdaptorClass = applicationAdaptorClass;
	}

	public String getApplicationAdaptorClass() {
	    return applicationAdaptorClass;
    }
	
	
	public static UserSpecification fromConfigurationModel(OntModel cmmConfigModel, Resource userSpec) {
		AgentAddress agentAddress = AgentSpecification.getAddressFromConfig(cmmConfigModel, userSpec);
		
		String applicationAdaptorClass = getAdaptorClass(cmmConfigModel, 
			userSpec.getPropertyResourceValue(OrgConf.hasApplicationInterfacingAdaptor));
		
		// We don't have any Control Policy for the CtxUser for now
		return new UserSpecification(agentAddress, null, applicationAdaptorClass);
	}
	
	private static String getAdaptorClass(OntModel cmmConfigModel, Resource assertionAdaptorRes) {
    	return assertionAdaptorRes.getProperty(OrgConf.hasQualifiedName).getString();
    }
}
