package org.aimas.ami.cmm.agent.config;

import java.util.LinkedList;
import java.util.List;

import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class ContextDomain {
	private Resource domainDimension;
	private Resource domainEntity;
	private Resource domainValue;
	
	private List<String> domainModelPartitionDocs;

	public ContextDomain(Resource domainDimension, Resource domainEntity,
            Resource domainValue, List<String> domainModelPartitionDocs) {
	    this.domainDimension = domainDimension;
	    this.domainEntity = domainEntity;
	    this.domainValue = domainValue;
	    this.domainModelPartitionDocs = domainModelPartitionDocs;
    }

	public ContextDomain(List<String> domainModelPartitionDocs) {
	    this(null, null, null, domainModelPartitionDocs);
    }

	public Resource getDomainDimension() {
		return domainDimension;
	}
	
	public boolean hasDomainDimension() {
		return domainDimension != null;
	}

	public Resource getDomainEntity() {
		return domainEntity;
	}
	
	public boolean hasDomainEntity() {
		return domainEntity != null;
	}

	public Resource getDomainValue() {
		return domainValue;
	}
	
	public boolean hasDomainValue() {
		return domainValue != null;
	}

	public List<String> getDomainModelPartitionDocs() {
		return domainModelPartitionDocs;
	}
	
	
	public static ContextDomain fromConfigurationModel(OntModel cmmConfigModel, Resource contextDomainResource) {
		Resource domainDimension = cmmConfigModel.getProperty(contextDomainResource, OrgConf.hasDimension).getResource();
		Resource domainEntity = cmmConfigModel.getProperty(contextDomainResource, OrgConf.hasDomainEntity).getResource();
		Resource domainValue = cmmConfigModel.getProperty(contextDomainResource, OrgConf.hasDomainValue).getResource();
	
		StmtIterator partitionIterator = cmmConfigModel.listStatements(contextDomainResource, OrgConf.hasModelPartition, (RDFNode)null);
		
		List<String> domainModelPartitionDocs = new LinkedList<String>();
		for (;partitionIterator.hasNext();) {
			Resource modelPartition = partitionIterator.next().getResource();
			Resource partitionDocument = modelPartition.getPropertyResourceValue(OrgConf.hasPartitionDocument);
			
			Statement docFileStmt = partitionDocument.getProperty(OrgConf.documentPath);
			if (docFileStmt != null) {
				domainModelPartitionDocs.add(docFileStmt.getString());
			}
			else {
				docFileStmt = partitionDocument.getProperty(OrgConf.documentURI);
				domainModelPartitionDocs.add(docFileStmt.getString());
			}
		}
		
		return new ContextDomain(domainDimension, domainEntity, domainValue, domainModelPartitionDocs);
	}
}
