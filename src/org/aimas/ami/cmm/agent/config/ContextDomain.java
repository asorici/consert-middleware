package org.aimas.ami.cmm.agent.config;

import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

public class ContextDomain {
	private Resource domainDimension;
	private Resource domainRangeEntity;
	private Resource domainRangeValue;
	
	private ContextModelDefinition domainModelDefinition;

	public ContextDomain(Resource domainDimension, Resource domainEntity,
            Resource domainValue, ContextModelDefinition domainModelDefinition) {
	    this.domainDimension = domainDimension;
	    this.domainRangeEntity = domainEntity;
	    this.domainRangeValue = domainValue;
	    this.domainModelDefinition = domainModelDefinition;
    }

	public ContextDomain(ContextModelDefinition domainModelDefinition) {
	    this(null, null, null, domainModelDefinition);
    }

	public Resource getDomainDimension() {
		return domainDimension;
	}
	
	public boolean hasDomainDimension() {
		return domainDimension != null;
	}

	public Resource getDomainEntity() {
		return domainRangeEntity;
	}
	
	public boolean hasDomainEntity() {
		return domainRangeEntity != null;
	}

	public Resource getDomainValue() {
		return domainRangeValue;
	}
	
	public boolean hasDomainValue() {
		return domainRangeValue != null;
	}

	public ContextModelDefinition getDomainContextModelDefinition() {
		return domainModelDefinition;
	}
	
	
	public static ContextDomain fromConfigurationModel(OntModel cmmConfigModel, Resource contextDomainResource) {
		Resource domainDimension = cmmConfigModel.getProperty(contextDomainResource, OrgConf.hasDomainDimension).getResource();
		Resource domainRangeEntity = cmmConfigModel.getProperty(contextDomainResource, OrgConf.hasDomainRangeEntity).getResource();
		Resource domainRangeValue = cmmConfigModel.getProperty(contextDomainResource, OrgConf.hasDomainRangeValue).getResource();
		
		Resource domainModelRes = cmmConfigModel.getProperty(contextDomainResource, OrgConf.hasContextModel).getResource();
		ContextModelDefinition contextModelDefinition = ContextModelDefinition.fromConfigurationModel(cmmConfigModel, domainModelRes);
		
		return new ContextDomain(domainDimension, domainRangeEntity, domainRangeValue, contextModelDefinition);
	}
}
