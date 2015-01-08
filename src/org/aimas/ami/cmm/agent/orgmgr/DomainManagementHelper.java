package org.aimas.ami.cmm.agent.orgmgr;

import jade.core.AID;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aimas.ami.cmm.agent.config.ContextDomainSpecification;
import org.aimas.ami.cmm.agent.onto.QueryBaseItem;
import org.aimas.ami.cmm.agent.onto.UserQuery;
import org.aimas.ami.cmm.agent.onto.impl.DefaultQueryBaseItem;
import org.aimas.ami.cmm.api.CMMConfigException;
import org.aimas.ami.cmm.exceptions.DomainValueNotFoundException;
import org.aimas.ami.contextrep.model.exceptions.ContextModelConfigException;
import org.aimas.ami.contextrep.utils.ContextModelLoader;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

public class DomainManagementHelper {
	
	private OrgMgr orgMgr;
	
	private Property domainDimension;
	private Resource domainRangeEntity;
	private Resource domainRangeValue;
	
	private Property domainHierarchyProperty;
	private OntModel domainHierarchyModel;
	
	private Map<DomainInfoWrapper, AID> childDomainsMap;
	private Map<DomainInfoWrapper, AID> rootDomainsMap;
	
	private DomainInfoWrapper parentDomainInfo;
	
	public DomainManagementHelper(OrgMgr managerAgent, ContextDomainSpecification domainSpec) throws CMMConfigException {
		this.orgMgr = managerAgent;
		
		// Configure our own domain attributes
		domainDimension = domainSpec.getDomainDimension().as(Property.class);
		domainRangeEntity = domainSpec.getDomainEntity();
		domainRangeValue = domainSpec.getDomainValue();
		
		// Configure the domain hierarchy attributes, if they are defined
		if (domainSpec.hasDomainHierarchy()) {
			domainHierarchyProperty = domainSpec.getDomainHierarchyProperty();
			
			String domainHierarchyDocument = domainSpec.getDomainHierarchyDocument();
			OntModel domainHierarchyRawModel = managerAgent.configurationLoader.load(domainHierarchyDocument);
			
			// The above is only the raw domainHierarchyModel. To obtain a model supporting inference, we have
			// to add a scheme to it which is the CONSERT core model
			try {
	            ContextModelLoader contextModelLoader = new ContextModelLoader(managerAgent.configurationLoader.getResourceManager(), 
	            		domainSpec.getDomainContextModelDefinition().getContextModelFileDictionary());
	            Model domainCoreModel = contextModelLoader.getCoreContextModel();
	            domainHierarchyRawModel.addSubModel(domainCoreModel, false);
	            
	            domainHierarchyModel = contextModelLoader.getRDFSInferenceModel(domainHierarchyRawModel);
            }
            catch (ContextModelConfigException e) {
	            throw new CMMConfigException("Could not propertly load Context Model File while attempting "
	            		+ "to build and infer ContextDomain hierarchy model.", e);
            }
			
			
			// If we have a domainHierarchyModel, verify that it actually contains our own domainRangeValue
			if (domainRangeValue == null) {
				throw new CMMConfigException("We have a domain hierarchy model, but NO own domain value.");
			}
			
			if (!domainHierarchyModel.contains(domainRangeValue, RDF.type, domainRangeEntity)) {
				throw new CMMConfigException("We have a domain hierarchy model, but our own domain value is NOT included in it.");
			}
		}
		
		// Instantiate child domains map
		childDomainsMap = new HashMap<DomainInfoWrapper, AID>();
		rootDomainsMap = new HashMap<DomainInfoWrapper, AID>();
	}
	
	public boolean hasDimensionalityConfig() {
		return domainDimension != null;
	}
	
	public boolean hasDomainHierarchyConfig() {
		return domainHierarchyModel != null;
	}
	
	public Property getDomainDimension() {
		return domainDimension;
	}

	public Resource getDomainRangeEntity() {
		return domainRangeEntity;
	}

	public Resource getDomainRangeValue() {
		return domainRangeValue;
	}
	
	// OrgMgr relations management
	/////////////////////////////////////////////////////////////////////////////
	public void setParentManagerDomain(String domainRangeEntityURI, String domainRangeValueURI, AID parentQueryResponder) {
		Resource parentDomainRangeEntity = ResourceFactory.createResource(domainRangeEntityURI);
		Resource parentDomainRangeValue = ResourceFactory.createResource(domainRangeValueURI);
		
		parentDomainInfo = new DomainInfoWrapper(parentDomainRangeEntity, parentDomainRangeValue, parentQueryResponder);
	}
	
	public void registerChildManager(String childDomainEntityURI, String childDomainValueURI, 
			AID managerAgent, AID childQueryResponder) {
		Resource childDomainEntity = ResourceFactory.createResource(childDomainEntityURI);
		Resource childDomainValue = ResourceFactory.createResource(childDomainValueURI);
		
		DomainInfoWrapper wrapper = new DomainInfoWrapper(childDomainEntity, childDomainValue, childQueryResponder);
		childDomainsMap.put(wrapper, managerAgent);
	}
	
	public void registerRootManager(String domainEntityURI, String domainValueURI, 
			AID managerAgent, AID rootQueryResponder) {
		Resource rootDomainEntity = ResourceFactory.createResource(domainEntityURI);
		Resource rootDomainValue = ResourceFactory.createResource(domainValueURI);
		
		DomainInfoWrapper wrapper = new DomainInfoWrapper(rootDomainEntity, rootDomainValue, rootQueryResponder);
		rootDomainsMap.put(wrapper, managerAgent);
	}
	
	public AID getParentQueryHandler() {
		return parentDomainInfo.domainQueryResponder;
	}
	
	// Domain Matching
	/////////////////////////////////////////////////////////////////////////////
	public boolean matchesDomain(Resource domainValue) {
		return domainValue.equals(domainRangeValue);
	}
	
	
	private boolean domainOnPathTo(Resource searchedDomainValue, Resource pathDomainValue)  {
		// Perform a lookup along the hierarchy chain, starting with the given domainValue
		Resource currentDomainVal = searchedDomainValue;
		while(true) {
			if (currentDomainVal.equals(pathDomainValue)) {
				return true;
			}
			
			Statement nextDomainStmt = domainHierarchyModel.getProperty(currentDomainVal, domainHierarchyProperty);
			if (nextDomainStmt != null) {
				currentDomainVal = nextDomainStmt.getResource();
			}
			else {
				break;
			}
		}
		
		return false;
	}
	
	public boolean domainLowerInHierarchy(Resource domainValue) throws DomainValueNotFoundException {
		if (!domainHierarchyModel.contains(domainValue, RDF.type, domainRangeEntity)) {
			throw new DomainValueNotFoundException(domainValue);
		}
		
		// Perform a lookup along the hierarchy chain, starting with the given domainValue
		Resource currentDomainVal = domainValue;
		while(true) {
			if (currentDomainVal.equals(domainRangeValue)) {
				return true;
			}
			
			Statement nextDomainStmt = domainHierarchyModel.getProperty(currentDomainVal, domainHierarchyProperty);
			if (nextDomainStmt != null) {
				currentDomainVal = nextDomainStmt.getResource();
			}
			else {
				break;
			}
		}
		
		return false;
	}
	
	public boolean domainHigherInHierarchy(Resource domainValue) throws DomainValueNotFoundException {
		if (!domainHierarchyModel.contains(domainValue, RDF.type, domainRangeEntity)) {
			throw new DomainValueNotFoundException(domainValue);
		}
		
		// Perform a lookup along the hierarchy chain, starting with our given domainRangeValue
		Resource currentDomainVal = domainRangeValue;
		while(true) {
			if (currentDomainVal.equals(domainValue)) {
				return true;
			}
			
			Statement nextDomainStmt = domainHierarchyModel.getProperty(currentDomainVal, domainHierarchyProperty);
			if (nextDomainStmt != null) {
				currentDomainVal = nextDomainStmt.getResource();
			}
			else {
				break;
			}
		}
		
		return false;
	}
	
	public List<QueryBaseItem> resolveQueryBase(UserQuery queryDesc, AID receivedFromAgent) 
			throws DomainValueNotFoundException {
		List<QueryBaseItem> queryBaseItems = new LinkedList<QueryBaseItem>();
		
		String queryLowerBoundURI = queryDesc.getDomain_lower_bound();
        String queryUpperBoundURI = queryDesc.getDomain_upper_bound();
        
        if (hasDomainHierarchyConfig()) {
        	Resource queryUpperBound = ResourceFactory.createResource(queryUpperBoundURI);
        	
        	if (queryLowerBoundURI.isEmpty()) {
        		// We are dealing with an UPPER LIMIT BROADCAST
        		if (matchesDomain(queryUpperBound) || domainHigherInHierarchy(queryUpperBound)) {
        			// When the upper limit is us or above us, add us and the children except the receivedFromAgent
        			QueryBaseItem qbItem = new DefaultQueryBaseItem();
        			qbItem.setQueryHandler(orgMgr.agentManager.getManagedQueryHandlers().get(0).getAgentAID());
        			qbItem.setQueryUpperBound(queryUpperBoundURI);
        			qbItem.setQueryLowerBound(queryLowerBoundURI);
        			queryBaseItems.add(qbItem);
        			
        			// Also add all child managers, except the receivedFromAgent
        			for (DomainInfoWrapper domainWrapper : childDomainsMap.keySet()) {
        				if (!domainWrapper.domainQueryResponder.equals(receivedFromAgent)) {
        					QueryBaseItem childItem = new DefaultQueryBaseItem();
        					childItem.setQueryHandler(domainWrapper.domainQueryResponder);
        					
        					// This is where we modify the upper bound of an upper broadcast query, when
        					// we "go down" a branch
        					childItem.setQueryUpperBound(domainWrapper.domainRangeValue.getURI());
        					childItem.setQueryLowerBound(queryLowerBoundURI);
        					queryBaseItems.add(childItem);
        				}
        			}
        			
        			// If we are not the upper limit and we have a parent, add the parent as well
        			if (!matchesDomain(queryUpperBound) && parentDomainInfo != null) {
        				QueryBaseItem parentQBItem = new DefaultQueryBaseItem();
            			parentQBItem.setQueryHandler(getParentQueryHandler());
            			parentQBItem.setQueryUpperBound(queryUpperBoundURI);
            			parentQBItem.setQueryLowerBound(queryLowerBoundURI);
            			queryBaseItems.add(parentQBItem);
        			}
        		}
        		else {
        			throw new DomainValueNotFoundException(queryUpperBound);
        		}
        	}
        	else {
        		// We are dealing with an double-bounded domain query, but for now
        		// we only handle an exact domain match
        		Resource queryLowerBound = ResourceFactory.createResource(queryLowerBoundURI);
        		if (queryLowerBound.equals(queryUpperBound)) {
        			if (domainHigherInHierarchy(queryUpperBound) && parentDomainInfo != null) {
        				QueryBaseItem parentQBItem = new DefaultQueryBaseItem();
            			parentQBItem.setQueryHandler(getParentQueryHandler());
            			parentQBItem.setQueryUpperBound(queryUpperBoundURI);
            			parentQBItem.setQueryLowerBound(queryLowerBoundURI);
            			queryBaseItems.add(parentQBItem);
        			}
        			else if (domainLowerInHierarchy(queryLowerBound)) {
        				// Determine which child manager is needed
        				for (DomainInfoWrapper domainWrapper : childDomainsMap.keySet()) {
        					// We only choose the child manager which is "on path" from the searched domain
        					if (domainOnPathTo(queryLowerBound, domainWrapper.domainRangeValue)) {
	        					QueryBaseItem childItem = new DefaultQueryBaseItem();
	        					childItem.setQueryHandler(domainWrapper.domainQueryResponder);
	        					childItem.setQueryUpperBound(queryUpperBoundURI);
	        					childItem.setQueryLowerBound(queryLowerBoundURI);
	        					queryBaseItems.add(childItem);
        					}
        				}
        			}
        			else {
        				throw new DomainValueNotFoundException(queryLowerBound);
        			}
        		}
        	}
        }
		
		return queryBaseItems;
	}
	
	// Helper Class
	/////////////////////////////////////////////////////////////////////////////
	private class DomainInfoWrapper {
		Resource domainRangeEntity;
		Resource domainRangeValue;
		AID domainQueryResponder;
		
		DomainInfoWrapper(Resource domainRangeEntity, Resource domainRangeValue, AID domainQueryResponder) {
	        this.domainRangeEntity = domainRangeEntity;
	        this.domainRangeValue = domainRangeValue;
	        this.domainQueryResponder = domainQueryResponder;
        }

		@Override
        public int hashCode() {
	        return domainRangeValue.getURI().hashCode();
        }

		@Override
        public boolean equals(Object obj) {
	        if (this == obj) {
		        return true;
	        }
	        
	        DomainInfoWrapper other = (DomainInfoWrapper) obj;
	        
	        if (domainRangeValue == null) {
		        if (other.domainRangeValue != null) {
			        return false;
		        }
	        }
	        else if (!domainRangeValue.equals(other.domainRangeValue)) {
		        return false;
	        }
	        
	        return true;
        }		
	}
}
