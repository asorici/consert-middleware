package org.aimas.ami.cmm.agent.config;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class CoordinatorSpecification extends AgentSpecification {
	
	private String domainPartitionDoc;
	private String engineAdaptorClass;
	private AgentAddress managerAddress;
	
	public CoordinatorSpecification(AgentAddress agentAddress, AgentPolicy controlPolicy, 
			String domainPartitionDoc, String engineAdaptorClass, AgentAddress managerAddress) {
	    
		super(agentAddress, AgentType.CTX_COORD, controlPolicy);
		this.domainPartitionDoc = domainPartitionDoc;
		this.engineAdaptorClass = engineAdaptorClass;
		this.managerAddress = managerAddress;
    }
	
	public String getDomainPartitionDoc() {
		return domainPartitionDoc;
	}

	public String getEngineAdaptorClass() {
		return engineAdaptorClass;
	}

	public AgentAddress getManagerAddress() {
		return managerAddress;
	}
	
	
	public static CoordinatorSpecification fromConfigurationModel(OntModel cmmConfigModel, Resource coordSpec) {
		AgentAddress agentAddress = AgentSpecification.getAddressFromConfig(cmmConfigModel, coordSpec);
		AgentPolicy controlPolicy = AgentSpecification.getPolicyFromConfig(cmmConfigModel, coordSpec);
		
		String domainPartitionDoc = getDomainParitionDoc(cmmConfigModel, 
			coordSpec.getPropertyResourceValue(OrgConf.handlesModelPartition));
		
		String engineAdaptorClass = getAdaptorClass(cmmConfigModel, coordSpec.getPropertyResourceValue(OrgConf.hasEngineAdaptor));
		
		AgentAddress managerAddress = AgentAddress.fromConfigurationModel(cmmConfigModel, 
			coordSpec.getPropertyResourceValue(OrgConf.assignedOrgManager));	
		
		return new CoordinatorSpecification(agentAddress, controlPolicy, domainPartitionDoc, 
			engineAdaptorClass, managerAddress);
	}
	
	
	private static String getAdaptorClass(OntModel cmmConfigModel, Resource assertionAdaptorRes) {
    	return assertionAdaptorRes.getProperty(OrgConf.hasQualifiedName).getString();
    }
	
	
	private static String getDomainParitionDoc(OntModel cmmConfigModel, Resource modelPartitionRes) {
    	Resource partitionDocument = modelPartitionRes.getPropertyResourceValue(OrgConf.hasPartitionDocument);
    	
    	Statement docFileStmt = partitionDocument.getProperty(OrgConf.documentPath);
		if (docFileStmt != null) {
			return docFileStmt.getString();
		}
		else {
			docFileStmt = partitionDocument.getProperty(OrgConf.documentURI);
			return docFileStmt.getString();
		}
    }
}
