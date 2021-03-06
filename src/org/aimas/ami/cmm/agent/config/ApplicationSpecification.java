package org.aimas.ami.cmm.agent.config;

import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class ApplicationSpecification {
	public static enum DeploymentType {
		CentralizedLocal, DecentralizedHierarchical;
		
		public static DeploymentType getFromResource(Resource typeResource) {
			if (typeResource.equals(OrgConf.DecentralizedHierarchical)) {
				return DecentralizedHierarchical;
			}
			
			return CentralizedLocal;
		}
	}
	
	private String appIdentifier;
	private DeploymentType appDeploymentType;
	private ContextDomain localContextDomain;
	
	private CMMAgentContainer appAgentContainer; 
	
	public ApplicationSpecification(String appIdentifier, DeploymentType appDeploymentType, 
			ContextDomain localContextDomain, CMMAgentContainer appAgentContainer) {
	    this.appIdentifier = appIdentifier;
	    this.appDeploymentType = appDeploymentType;
	    this.localContextDomain = localContextDomain;
	    this.appAgentContainer = appAgentContainer;
    }

	public String getAppIdentifier() {
		return appIdentifier;
	}

	public DeploymentType getAppDeploymentType() {
		return appDeploymentType;
	}

	public ContextDomain getLocalContextDomain() {
		return localContextDomain;
	}
		
	public CMMAgentContainer getAppAgentContainer() {
		return appAgentContainer;
	}
	
	
	public static ApplicationSpecification fromConfigurationModel(OntModel cmmConfigModel) {
		Resource appSpec = cmmConfigModel.listResourcesWithProperty(RDF.type, OrgConf.ApplicationSpec).next();
		
		String appIdentifier = appSpec.getProperty(OrgConf.appIdentificationName).getString();
		DeploymentType appDeploymentType = DeploymentType.getFromResource(appSpec.getPropertyResourceValue(OrgConf.appDeploymentType));
		ContextDomain localContextDomain = ContextDomain.fromConfigurationModel(cmmConfigModel, 
				appSpec.getPropertyResourceValue(OrgConf.hasContextDomain));
		
		CMMAgentContainer appAgentContainer = CMMAgentContainer.fromConfigurationModel(cmmConfigModel, 
				appSpec.getPropertyResourceValue(OrgConf.hasAgentContainer));
		
		return new ApplicationSpecification(appIdentifier, appDeploymentType, localContextDomain, appAgentContainer);
	}
}
