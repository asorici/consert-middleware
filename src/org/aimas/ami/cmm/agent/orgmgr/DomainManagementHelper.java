package org.aimas.ami.cmm.agent.orgmgr;

import jade.core.AID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aimas.ami.cmm.agent.config.ContextDomainSpecification;
import org.aimas.ami.cmm.agent.config.ManagerSpecification.ManagerType;
import org.aimas.ami.cmm.agent.onto.BroadcastBaseItem;
import org.aimas.ami.cmm.agent.onto.QueryBaseItem;
import org.aimas.ami.cmm.agent.onto.UserQuery;
import org.aimas.ami.cmm.agent.onto.impl.DefaultBroadcastBaseItem;
import org.aimas.ami.cmm.agent.onto.impl.DefaultQueryBaseItem;
import org.aimas.ami.cmm.api.CMMConfigException;
import org.aimas.ami.cmm.exceptions.DomainTypeNotFoundException;
import org.aimas.ami.cmm.exceptions.DomainValueNotFoundException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class DomainManagementHelper {
	
	private OrgMgr orgMgr;
	
	private Property domainDimension;
	private Resource domainRangeEntity;
	private Resource domainRangeValue;
	
	private Property domainHierarchyProperty;
	private OntModel domainHierarchyModel;
	private List<Resource> domainTypeOrder;
	
	private Map<DomainInfo, DomainAgentsInfo> childDomainsMap;
	private Map<DomainInfo, DomainAgentsInfo> rootDomainsMap;
	private List<AID> rootDomainQueryHandlers;
	private List<AID> rootDomainCoordinators;
	
	private DomainInfo parentDomainInfo;
	private DomainAgentsInfo parentDomainAgentsInfo;
	
	public DomainManagementHelper(OrgMgr managerAgent, ContextDomainSpecification domainSpec) throws CMMConfigException {
		this.orgMgr = managerAgent;
		
		// We only need to check things if we indeed have ContextDimension and ContextDomain configurations
		if (domainSpec.hasDomainDimension()) {
		
			// Configure our own domain attributes
			domainDimension = domainSpec.getDomainDimension().as(Property.class);
			domainRangeEntity = domainSpec.getDomainEntity();
			domainRangeValue = domainSpec.getDomainValue();
			
			// Configure the domain hierarchy attributes, if they are defined
			if (domainSpec.hasDomainHierarchy()) {
				domainHierarchyProperty = domainSpec.getDomainHierarchyProperty();
				
				String domainHierarchyDocument = domainSpec.getDomainHierarchyDocument();
				Model domainHierarchyModel = managerAgent.configurationLoader.loadDocumentModel(domainHierarchyDocument);
				
				// The above is only the raw domainHierarchyModel. To obtain a model supporting inference, we have
				// to add a scheme to it which is the CONSERT core model
				/*
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
				*/
				
				
				// If we have a domainHierarchyModel, verify that it actually contains our own domainRangeValue
				if (domainRangeValue == null) {
					throw new CMMConfigException("We have a domain hierarchy model, but NO own domain value.");
				}
				
				if (!domainHierarchyModel.contains(domainRangeValue, RDF.type, (RDFNode)null)) {
					throw new CMMConfigException("We have a domain hierarchy model, "
							+ "but our own domain value (" + domainRangeValue + ") is NOT included in it.");
				}
				
				// If we have a domainHierarchyModel and our domain value is included therein, compute the
				domainTypeOrder = getOrderedDomainTypeList(domainHierarchyProperty, domainHierarchyModel);
			}
			
			// Instantiate child domains map
			childDomainsMap = new HashMap<DomainInfo, DomainAgentsInfo>();
			rootDomainsMap = new HashMap<DomainInfo, DomainAgentsInfo>();
			rootDomainCoordinators = new ArrayList<AID>();
			rootDomainQueryHandlers = new ArrayList<AID>();
		}
	}
	
	private List<Resource> getOrderedDomainTypeList(Property domainHierarchyProperty, Model domainHierarchyModel) {
	    /* 
	     * We know that the domainHierarchyModel contains a list of domain definitions (i.e. <domainVal> rdf:type <domainType> statements)
	     * and domain inclusion specifications (i.e. <domainVal> <domainHierarchyProperty> <domainVal>).
	     * Therefore we index all the domain definitions, find the root domain for this hierarchy model and perform a BFS traversal of the tree to mark the
	     * different levels of each domain-type: the traversal defines the order of the domain types.
	     */
	    StmtIterator it = domainHierarchyModel.listStatements(null, RDF.type, (RDFNode)null);
	    
	    Set<Resource> domainValueSet = new HashSet<Resource>();
	    Set<Resource> domainTypeSet = new HashSet<Resource>();
	    
	    final Map<Resource, Integer> domainTypeLevels = new HashMap<Resource, Integer>();
	    
	    for ( ; it.hasNext(); ) {
	    	Statement s = it.next();
	    	Resource domainValue = s.getSubject();
	    	Resource domainType = s.getResource();
	    	
	    	domainValueSet.add(domainValue);
	    	domainTypeSet.add(domainType);
	    }
	    
	    // The domainHierarchyModel will contain only one hierarchy setup (that is, up to the current root domain).
	    // Therefore, to find the domain value root we need to search for the one domainValue which has 
	    // one domainHierarchyProperty coming in, and none coming out  
	    Resource rootDomainVal = null;
	    for (Resource domainVal : domainValueSet) {
	    	if (domainHierarchyModel.contains(null, domainHierarchyProperty, domainVal) && !domainHierarchyModel.contains(domainVal, domainHierarchyProperty, (RDFNode)null)) {
	    		rootDomainVal = domainVal;
	    		break;
	    	}
	    }
	    
	    System.out.println("["+getClass().getSimpleName()+"] DOMAIN VALUE SET = " + domainValueSet);
	    System.out.println("["+getClass().getSimpleName()+"] ROOT DOMAIN = " + rootDomainVal);
	    
	    if (rootDomainVal != null) {
		    
	    	// 1) do a BFS of the tree and mark/overwrite the "levels" for each domainType
	    	LinkedList<Resource> visitList = new LinkedList<Resource>();
		    visitList.add(rootDomainVal);
		    int level = 1;
		    
		    while (!visitList.isEmpty()) {
		    	Resource domainVal = visitList.poll();
		    	Resource domainType = domainHierarchyModel.getProperty(domainVal, RDF.type).getResource();
		    	
		    	// update levels
		    	int dtLevel = domainTypeLevels.containsKey(domainType) ? domainTypeLevels.get(domainType) : 0;
		    	if (dtLevel == 0 || dtLevel > level) {
		    		domainTypeLevels.put(domainType, level);
		    	}
		    	
		    	// continue BFS
		    	StmtIterator domainValIt = domainHierarchyModel.listStatements(null, domainHierarchyProperty, domainVal);
		    	for ( ; domainValIt.hasNext(); ) {
		    		Resource dVal = domainValIt.next().getSubject();
		    		visitList.offer(dVal);
		    	}
		    }
		    
		    // 2) use the levels to sort the domainTypes
		    List<Resource> domainTypeList = new LinkedList<Resource>(domainTypeSet);
		    Collections.sort(domainTypeList, new Comparator<Resource>() {
				@Override
                public int compare(Resource r1, Resource r2) {
	                int level1 = domainTypeLevels.get(r1);
	                int level2 = domainTypeLevels.get(r2);
	                
	                return level1 - level2;
                }
		    });
		    
		    return domainTypeList;
	    }
	    
	    
	    return null;
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
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void setParentManagerDomain(String domainRangeEntityURI, String domainRangeValueURI, 
			AID parentManager, AID parentCoordinator, AID parentQueryHandler) {
		
		Resource parentDomainRangeEntity = ResourceFactory.createResource(domainRangeEntityURI);
		Resource parentDomainRangeValue = ResourceFactory.createResource(domainRangeValueURI);
		
		parentDomainInfo = new DomainInfo(parentDomainRangeEntity, parentDomainRangeValue);
		parentDomainAgentsInfo = new DomainAgentsInfo(parentManager, parentCoordinator, parentQueryHandler);
	}
	
	public void registerChildManager(String childDomainEntityURI, String childDomainValueURI, 
			AID childManagerAgent, AID childCoordinator, AID childQueryHandler) {
		Resource childDomainEntity = ResourceFactory.createResource(childDomainEntityURI);
		Resource childDomainValue = ResourceFactory.createResource(childDomainValueURI);
		
		DomainInfo childDomainInfo = new DomainInfo(childDomainEntity, childDomainValue);
		DomainAgentsInfo childDomainAgentsInfo = new DomainAgentsInfo(childManagerAgent, childCoordinator, childQueryHandler);
		childDomainsMap.put(childDomainInfo, childDomainAgentsInfo);
	}
	
	public void registerRootManager(String domainEntityURI, String domainValueURI, 
			AID rootManagerAgent, AID rootCoordinator, AID rootQueryHandler) {
		Resource rootDomainEntity = ResourceFactory.createResource(domainEntityURI);
		Resource rootDomainValue = ResourceFactory.createResource(domainValueURI);
		
		DomainInfo rootDomainInfo = new DomainInfo(rootDomainEntity, rootDomainValue);
		DomainAgentsInfo rootDomainAgentsInfo = new DomainAgentsInfo(rootManagerAgent, rootCoordinator, rootQueryHandler);
		
		rootDomainsMap.put(rootDomainInfo, rootDomainAgentsInfo);
		rootDomainCoordinators.add(rootCoordinator);
		rootDomainQueryHandlers.add(rootQueryHandler);
	}
	
	public AID getParentCoordinator() {
		return parentDomainAgentsInfo.getCoordinator();
	}
	
	public AID getParentQueryHandler() {
		return parentDomainAgentsInfo.getQueryHandler();
	}
	
	// Domain Matching
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
	
	// Resolve Query Base Calls
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public List<QueryBaseItem> resolveExactDomainQueryBase(UserQuery queryDesc, AID receivedFromAgent) 
			throws DomainValueNotFoundException {
		List<QueryBaseItem> queryBaseItems = new LinkedList<QueryBaseItem>();
		
		String queryLowerBoundURI = queryDesc.getDomain_lower_bound();
        String queryUpperBoundURI = queryDesc.getDomain_upper_bound();
        
        if (queryLowerBoundURI.equals(queryUpperBoundURI)) {
        	Resource searchedDomainValue = ResourceFactory.createResource(queryUpperBoundURI);
        	
        	if (hasDomainHierarchyConfig()) {
        		if (orgMgr.myType == ManagerType.Node) {
            		queryBaseItems.addAll(inspectHierarchy(searchedDomainValue));
            	}
            	else if (orgMgr.myType == ManagerType.Root) {
            		queryBaseItems.addAll(inspectHierarchy(searchedDomainValue));
            		if (queryBaseItems.isEmpty() && !rootDomainQueryHandlers.contains(receivedFromAgent)) {            			
            			for (AID rootQueryHandler : rootDomainQueryHandlers) {
            				QueryBaseItem rootItem = new DefaultQueryBaseItem();
            				rootItem.setQueryHandler(rootQueryHandler);
            				rootItem.setQueryUpperBound(searchedDomainValue.getURI());
            				rootItem.setQueryLowerBound(searchedDomainValue.getURI());
            				queryBaseItems.add(rootItem);
            			}
            		}
            	}
            }
        	else {
        		if (!rootDomainQueryHandlers.contains(receivedFromAgent)) {
        			for (AID rootQueryHandler : rootDomainQueryHandlers) {
        				QueryBaseItem rootItem = new DefaultQueryBaseItem();
        				rootItem.setQueryHandler(rootQueryHandler);
        				rootItem.setQueryUpperBound(searchedDomainValue.getURI());
        				rootItem.setQueryLowerBound(searchedDomainValue.getURI());
        				queryBaseItems.add(rootItem);
        			}
        		}
        	}
        }
		
		return queryBaseItems;
	}
	
	private List<QueryBaseItem> inspectHierarchy(Resource searchedDomainValue) throws DomainValueNotFoundException {
		List<QueryBaseItem> queryBaseItems = new LinkedList<QueryBaseItem>();
		
		if (domainLowerInHierarchy(searchedDomainValue)) {
			// Determine which child manager is needed
			for (DomainInfo childDomainInfo : childDomainsMap.keySet()) {
				// We only choose the child manager which is "on path" from the searched domain
				if (domainOnPathTo(searchedDomainValue, childDomainInfo.domainRangeValue)) {
					QueryBaseItem childItem = new DefaultQueryBaseItem();
					childItem.setQueryHandler(childDomainsMap.get(childDomainInfo).getQueryHandler());
					childItem.setQueryUpperBound(searchedDomainValue.getURI());
					childItem.setQueryLowerBound(searchedDomainValue.getURI());
					queryBaseItems.add(childItem);
				}
			}
		}
		else if (domainHigherInHierarchy(searchedDomainValue) || orgMgr.myType == ManagerType.Node) {
			QueryBaseItem parentQBItem = new DefaultQueryBaseItem();
			parentQBItem.setQueryHandler(getParentQueryHandler());
			parentQBItem.setQueryUpperBound(searchedDomainValue.getURI());
			parentQBItem.setQueryLowerBound(searchedDomainValue.getURI());
			queryBaseItems.add(parentQBItem);
		}
		
		return queryBaseItems;
	}
	
	
	public List<QueryBaseItem> resolveDomainRangeQueryBase(UserQuery queryDesc, AID receivedFromAgent) throws DomainTypeNotFoundException {
		List<QueryBaseItem> queryBaseItems = new LinkedList<QueryBaseItem>();
		
		String queryLowerBoundTypeURI = queryDesc.getDomain_lower_bound();
        String queryUpperBoundTypeURI = queryDesc.getDomain_upper_bound();
        
        if (hasDomainHierarchyConfig()) {
        	if (withinDomainTypeLimits(domainRangeEntity, queryLowerBoundTypeURI, queryUpperBoundTypeURI)) {
        		QueryBaseItem rootItem = new DefaultQueryBaseItem();
				rootItem.setQueryHandler(orgMgr.agentManager.getManagedQueryHandler(orgMgr.appSpecification.getAppIdentifier()).getAgentAID());
				rootItem.setQueryUpperBound(queryUpperBoundTypeURI);
				rootItem.setQueryLowerBound(queryLowerBoundTypeURI);
				queryBaseItems.add(rootItem);
        	}
        	
        	// propagate upwords
        	if (orgMgr.myType == ManagerType.Node) {
        		QueryBaseItem parentItem = new DefaultQueryBaseItem();
				parentItem.setQueryHandler(parentDomainAgentsInfo.getQueryHandler());
				parentItem.setQueryUpperBound(queryUpperBoundTypeURI);
				parentItem.setQueryLowerBound(queryLowerBoundTypeURI);
				queryBaseItems.add(parentItem);
        	}
        	else if (orgMgr.myType == ManagerType.Root) {
        		if (!rootDomainQueryHandlers.contains(receivedFromAgent)) {
        			for (AID rootQueryHandler : rootDomainQueryHandlers) {
        				QueryBaseItem rootItem = new DefaultQueryBaseItem();
        				rootItem.setQueryHandler(rootQueryHandler);
        				rootItem.setQueryUpperBound(queryUpperBoundTypeURI);
        				rootItem.setQueryLowerBound(queryLowerBoundTypeURI);
        				queryBaseItems.add(rootItem);
        			}
        		}
        	}
        	
        	// propagate downwords only if limit not exceeded
        	for (DomainInfo childDomainInfo : childDomainsMap.keySet()) {
        		if (withinDomainTypeLimits(childDomainInfo.getDomainRangeEntity(), queryLowerBoundTypeURI, queryUpperBoundTypeURI)) {
        			QueryBaseItem childItem = new DefaultQueryBaseItem();
    				childItem.setQueryHandler(childDomainsMap.get(childDomainInfo).getQueryHandler());
    				childItem.setQueryUpperBound(queryUpperBoundTypeURI);
    				childItem.setQueryLowerBound(queryLowerBoundTypeURI);
    				queryBaseItems.add(childItem);
        		}
        	}
        }
        else {
        	if (withinDomainTypeLimits(domainRangeEntity, queryLowerBoundTypeURI, queryUpperBoundTypeURI)) {
        		QueryBaseItem rootItem = new DefaultQueryBaseItem();
				rootItem.setQueryHandler(orgMgr.agentManager.getManagedQueryHandler(orgMgr.appSpecification.getAppIdentifier()).getAgentAID());
				rootItem.setQueryUpperBound(queryUpperBoundTypeURI);
				rootItem.setQueryLowerBound(queryLowerBoundTypeURI);
				queryBaseItems.add(rootItem);
        	}
        	
        	if (!rootDomainQueryHandlers.contains(receivedFromAgent)) {
    			for (AID rootQueryHandler : rootDomainQueryHandlers) {
    				QueryBaseItem rootItem = new DefaultQueryBaseItem();
    				rootItem.setQueryHandler(rootQueryHandler);
    				rootItem.setQueryUpperBound(queryUpperBoundTypeURI);
    				rootItem.setQueryLowerBound(queryLowerBoundTypeURI);
    				queryBaseItems.add(rootItem);
    			}
    		}
        }
        
        return queryBaseItems;
	}
	
	
	public List<BroadcastBaseItem> resolveDomainRangeBroadcastBase(String domainTypeLowerLimitURI, String domainTypeUpperLimitURI, 
			AID receivedFromAgent) throws DomainTypeNotFoundException {
		
		List<BroadcastBaseItem> broadcastBaseItems = new LinkedList<BroadcastBaseItem>();
		
		if (hasDomainHierarchyConfig()) {
			AID myCoordinator = orgMgr.agentManager.getManagedCoordinator(orgMgr.appSpecification.getAppIdentifier()).getAgentAID();
	        
	        // first check for ourselves
	        if (withinDomainTypeLimits(domainRangeEntity, domainTypeLowerLimitURI, domainTypeUpperLimitURI)) {
        		BroadcastBaseItem myItem = new DefaultBroadcastBaseItem();
				myItem.setCoordinator(myCoordinator);
				myItem.setBroadcastLowerBound(domainTypeLowerLimitURI);
				myItem.setBroadcastUpperBound(domainTypeUpperLimitURI);
				
				broadcastBaseItems.add(myItem);
        	}
	        
	        // then check for parent
			if (orgMgr.myType == ManagerType.Node && !parentDomainAgentsInfo.getCoordinator().equals(receivedFromAgent) 
					&& withinDomainTypeLimits(parentDomainInfo.domainRangeEntity, domainTypeLowerLimitURI, domainTypeUpperLimitURI)) {
				
				BroadcastBaseItem parentItem = new DefaultBroadcastBaseItem();
				parentItem.setCoordinator(parentDomainAgentsInfo.getCoordinator());
				parentItem.setBroadcastLowerBound(domainTypeLowerLimitURI);
				parentItem.setBroadcastUpperBound(domainTypeUpperLimitURI);
				
				broadcastBaseItems.add(parentItem);
			}
			
			// finally check for children
			if (receivedFromAgent.equals(parentDomainAgentsInfo.getCoordinator()) || receivedFromAgent.equals(myCoordinator)) {
				for (DomainInfo childDomainInfo : childDomainsMap.keySet()) {
					
					if (withinDomainTypeLimits(childDomainInfo.domainRangeEntity, domainTypeLowerLimitURI, domainTypeUpperLimitURI)) {
						BroadcastBaseItem childItem = new DefaultBroadcastBaseItem();
						childItem.setCoordinator(childDomainsMap.get(childDomainInfo).getCoordinator());
						childItem.setBroadcastLowerBound(domainTypeLowerLimitURI);
						childItem.setBroadcastUpperBound(domainTypeUpperLimitURI);
						
						broadcastBaseItems.add(childItem);
					}
				}
			}
		}
		
		return broadcastBaseItems;
	}
	
	
	private boolean withinDomainTypeLimits(Resource searchedDomainType, String lowerDomainTypeLimitURI, String upperDomainTypeLimitURI) throws DomainTypeNotFoundException {
		if (domainTypeOrder != null) {
			int indexLower = 0;
			int indexUpper = domainTypeOrder.size();
	        
	        if (lowerDomainTypeLimitURI != null && !lowerDomainTypeLimitURI.isEmpty()){
	        	Resource lowerDomainTypeLimit = ResourceFactory.createResource(lowerDomainTypeLimitURI);
	        	
	        	indexLower = domainTypeOrder.indexOf(lowerDomainTypeLimit);
				if (indexLower < 0) {
					throw new DomainTypeNotFoundException(lowerDomainTypeLimit);
				}
	        }
			
	        if (lowerDomainTypeLimitURI != null && !lowerDomainTypeLimitURI.isEmpty()){
	        	Resource upperDomainTypeLimit = ResourceFactory.createResource(upperDomainTypeLimitURI);
				
	        	indexUpper = domainTypeOrder.indexOf(upperDomainTypeLimit);
				if (indexUpper < 0) {
					throw new DomainTypeNotFoundException(upperDomainTypeLimit);
				}
	        }
	        
			int indexSearched = domainTypeOrder.indexOf(searchedDomainType);
			if (indexSearched < 0) {
				throw new DomainTypeNotFoundException(searchedDomainType);
			}
			
			if (indexSearched >= indexUpper && indexSearched <= indexLower) {
				return true;
			}
		}
		
		return false;
	}
	
	
	// Helper Class
	/////////////////////////////////////////////////////////////////////////////
	private static class DomainInfo {
		Resource domainRangeEntity;
		Resource domainRangeValue;
		
		DomainInfo(Resource domainRangeEntity, Resource domainRangeValue) {
	        this.domainRangeEntity = domainRangeEntity;
	        this.domainRangeValue = domainRangeValue;
        }
		
		public Resource getDomainRangeEntity() {
			return domainRangeEntity;
		}
		
		public Resource getDomainRangeValue() {
			return domainRangeValue;
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
	        
	        DomainInfo other = (DomainInfo) obj;
	        
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
	
	
	private static class DomainAgentsInfo {
		AID orgMgr;
		AID coordinator;
		AID queryHandler;
		
		public DomainAgentsInfo(AID orgMgr, AID coordinator, AID queryHandler) {
	        this.orgMgr = orgMgr;
	        this.coordinator = coordinator;
	        this.queryHandler = queryHandler;
        }

		public AID getOrgMgr() {
			return orgMgr;
		}

		public AID getCoordinator() {
			return coordinator;
		}

		public AID getQueryHandler() {
			return queryHandler;
		}
	}
}
