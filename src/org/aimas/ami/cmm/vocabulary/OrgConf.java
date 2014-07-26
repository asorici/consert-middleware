package org.aimas.ami.cmm.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class OrgConf {
	/** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
	
	public final static String BASE_URI = "http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf";
	public final static String NS = BASE_URI + "#";
	
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    // Vocabulary classes
    /////////////////////
    
    // Configurables
    public final static Resource Configurable = m_model.createResource( NS + "Configurable" );
    public final static Resource AgentAddress = m_model.createResource( NS + "AgentAddress" );
    public final static Resource AgentContainer = m_model.createResource( NS + "AgentContainer" );
    public final static Resource AgentPolicy = m_model.createResource( NS + "AgentPolicy" );
    public final static Resource CtxSensorPolicy = m_model.createResource( NS + "CtxSensorPolicy" );
    public final static Resource AgentResource = m_model.createResource( NS + "AgentResource" );
    public final static Resource ContentDocument = m_model.createResource( NS + "ContentDocument" );
    public final static Resource ContextDomain = m_model.createResource( NS + "ContextDomain" );
    public final static Resource ContextModelPartition = m_model.createResource( NS + "ContextModelPartition" );
    public final static Resource DeploymentType = m_model.createResource( NS + "DeploymentType" );
    public final static Resource OrgMgrType = m_model.createResource( NS + "OrgMgrType" );
    
    // Specifications
    public final static Resource Specification = m_model.createResource( NS + "Specification" );
    public final static Resource AgentSpec = m_model.createResource( NS + "AgentSpec" );
    public final static Resource CtxCoordSpec = m_model.createResource( NS + "CtxCoordSpec" );
    public final static Resource CtxQueryHandlerSpec = m_model.createResource( NS + "CtxQueryHandlerSpec" );
    public final static Resource CtxSensorSpec = m_model.createResource( NS + "CtxSensorSpec" );
    public final static Resource CtxUserSpec = m_model.createResource( NS + "CtxUserSpec" );
    public final static Resource OrgMgrSpec = m_model.createResource( NS + "OrgMgrSpec" );
    public final static Resource ApplicationSpec = m_model.createResource( NS + "ApplicationSpec" );
    
    // Vocabulary properties
 	////////////////////////
    public final static Property hasControlPolicy = m_model.createProperty( NS + "hasControlPolicy" );
    public final static Property hasSensingPolicy = m_model.createProperty( NS + "hasSensingPolicy" );
    public final static Property hasModelPartition = m_model.createProperty( NS + "hasModelPartition" );
    public final static Property hasPartitionDocument = m_model.createProperty( NS + "hasPartitionDocument" );
    public final static Property hasPolicyDocument = m_model.createProperty( NS + "hasPolicyDocument" );
    
    public final static Property hasContextDomain = m_model.createProperty( NS + "hasContextDomain" );
    public final static Property hasDimension = m_model.createProperty( NS + "hasDimension" );
    public final static Property hasDomainEntity = m_model.createProperty( NS + "hasDomainEntity" );
    public final static Property hasDomainValue = m_model.createProperty( NS + "hasDomainValue" );
    
    public final static Property hasAgentAddress = m_model.createProperty( NS + "hasAgentAddress" );
    public final static Property agentName = m_model.createProperty( NS + "agentName" );
    public final static Property agentMTPHost = m_model.createProperty( NS + "agentMTPHost" );
    public final static Property agentMTPPort = m_model.createProperty( NS + "agentMTPPort" );
    public final static Property agentContainer = m_model.createProperty( NS + "agentContainer" );
    
    public final static Property assignedCoordinator = m_model.createProperty( NS + "assignedCoordinator" );
    public final static Property assignedOrgManager = m_model.createProperty( NS + "assignedOrgManager" );
    public final static Property hasManagerParent = m_model.createProperty( NS + "hasManagerParent" );
    public final static Property hasManagerRoot = m_model.createProperty( NS + "hasManagerRoot" );
    public final static Property knowsManagerRoot = m_model.createProperty( NS + "knowsManagerRoot" );
    public final static Property isPrimaryQueryHandler = m_model.createProperty( NS + "isPrimaryQueryHandler" );
    public final static Property hasManagerType = m_model.createProperty( NS + "hasManagerType" );
    
    public final static Property documentPath = m_model.createProperty( NS + "documentPath" );
    public final static Property documentURI = m_model.createProperty( NS + "documentURI" );
    public final static Property forContextAssertion = m_model.createProperty( NS + "forContextAssertion" );
    
    public final static Property hasEngineAdaptor = m_model.createProperty( NS + "hasEngineAdaptor" );
    public final static Property hasApplicationInterfacingAdaptor = m_model.createProperty( NS + "hasApplicationInterfacingAdaptor" );
    public final static Property hasQueryAdaptor = m_model.createProperty( NS + "hasQueryAdaptor" );
    public final static Property hasQualifiedName = m_model.createProperty( NS + "hasQualifiedName" );
    public final static Property usesAssertionAdaptor = m_model.createProperty( NS + "usesAssertionAdaptor" );
    public final static Property handlesModelPartition = m_model.createProperty( NS + "handlesModelPartition" );
    
    public final static Property appDeploymentType = m_model.createProperty( NS + "appDeploymentType" );
    public final static Property appIdentificationName = m_model.createProperty( NS + "appIdentificationName" );
    public final static Property hasAgentContainer = m_model.createProperty( NS + "hasAgentContainer" );
    public final static Property hasMainContainer = m_model.createProperty( NS + "hasMainContainer" );
    public final static Property isMainContainer = m_model.createProperty( NS + "isMainContainer" );
    public final static Property containerHost = m_model.createProperty( NS + "containerHost" );
    public final static Property containerPort = m_model.createProperty( NS + "containerPort" );
    public final static Property platformName = m_model.createProperty( NS + "platformName" );
    
    
    // Vocabulary Individuals
 	/////////////////////////
    public final static Resource CentralizedLocal = m_model.createResource( NS + "CentralizedLocal", DeploymentType);
    public final static Resource DecentralizedHierarchical = m_model.createResource( NS + "DecentralizedHierarchical", DeploymentType);
    public final static Resource NodeManager = m_model.createResource( NS + "NodeManager", OrgMgrType);
    public final static Resource RootManager = m_model.createResource( NS + "RootManager", OrgMgrType);
    
}
