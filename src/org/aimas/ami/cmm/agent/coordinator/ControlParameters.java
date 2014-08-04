package org.aimas.ami.cmm.agent.coordinator;

import java.util.HashMap;
import java.util.Map;

import org.aimas.ami.cmm.vocabulary.CoordConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class ControlParameters {
	private OntModel controlModel;
	private Resource controlPolicyRes;
	
	public ControlParameters(OntModel controlModel) {
	    this.controlModel = controlModel;
	    setup();
	}
	
	private void setup() {
		controlPolicyRes = controlModel.listResourcesWithProperty(RDF.type, CoordConf.ControlPolicy).next();
	}
	
	// DEFAULTS
	/////////////////////////////////////////////////////////////////////////////////////////
	public boolean defaultUpdateEnabled() {
		return controlPolicyRes.getProperty(CoordConf.enablesAssertionByDefault).getBoolean();
	}
	
	public long defaultRunWindow() {
		return controlPolicyRes.getProperty(CoordConf.hasDefaultRunWindow).getLong();
	}
	
	public int defaultOntReasoningInterval() {
		return controlPolicyRes.getProperty(CoordConf.hasDefaultOntReasoningInterval).getInt();
	}
	
	public int defaultTTL() {
		return controlPolicyRes.getProperty(CoordConf.hasDefaultTTLSpec).getInt();
	}
	
	public String defaultIntegrityResolutionStrategy() {
		return controlPolicyRes.getPropertyResourceValue(CoordConf.hasDefaultIntegrityResolution).getLocalName();
	}
	
	public String defaultUniquenessResolutionStrategy() {
		return controlPolicyRes.getPropertyResourceValue(CoordConf.hasDefaultUniquenessResolution).getLocalName();
	}
	
	public String inferenceSchedulingStrategy() {
		return controlPolicyRes.getPropertyResourceValue(CoordConf.hasInferenceSchedulingType).getLocalName();
	}
	
	// SPECIFICS
	/////////////////////////////////////////////////////////////////////////////////////////
	private Map<Resource, Boolean> specificAssertionEnabledMap;
	private Map<Resource, String> specificIntegrityResolutionStrategy;
	private Map<Resource, String> specificUniquenessResolutionStrategy;
	private Map<Resource, Integer> specificOntReasoningInterval;
	private Map<Resource, Integer> specificTTL;
	
	public Map<Resource, Boolean> specificUpdateEnabled() {
		if (specificAssertionEnabledMap == null) {
			specificAssertionEnabledMap = new HashMap<Resource, Boolean>();
		
			StmtIterator it = controlPolicyRes.listProperties(CoordConf.hasSpecificAssertionEnabling);
			for (;it.hasNext();) {
				Resource paramSpec = it.next().getResource();
				Resource assertionRes = paramSpec.getPropertyResourceValue(CoordConf.forContextAssertion);
				boolean value = paramSpec.getProperty(CoordConf.hasParameterValue).getBoolean();
				
				specificAssertionEnabledMap.put(assertionRes, value);
			}
		}
		
		return specificAssertionEnabledMap;
	}
	
	public Map<Resource, String> specificIntegrityResolutionStrategy() {
		if (specificIntegrityResolutionStrategy == null) {
			specificIntegrityResolutionStrategy = new HashMap<Resource, String>();
		
			StmtIterator it = controlPolicyRes.listProperties(CoordConf.hasSpecificIntegrityResolution);
			for (;it.hasNext();) {
				Resource paramSpec = it.next().getResource();
				Resource assertionRes = paramSpec.getPropertyResourceValue(CoordConf.forContextAssertion);
				String value = paramSpec.getPropertyResourceValue(CoordConf.hasParameterValue).getLocalName();
				
				specificIntegrityResolutionStrategy.put(assertionRes, value);
			}
		}
		
		return specificIntegrityResolutionStrategy;
	}
	
	public Map<Resource, String> specificUniquenessResolutionStrategy() {
		if (specificUniquenessResolutionStrategy == null) {
			specificUniquenessResolutionStrategy = new HashMap<Resource, String>();
		
			StmtIterator it = controlPolicyRes.listProperties(CoordConf.hasSpecificUniquenessResolution);
			for (;it.hasNext();) {
				Resource paramSpec = it.next().getResource();
				Resource assertionRes = paramSpec.getPropertyResourceValue(CoordConf.forContextAssertion);
				String value = paramSpec.getPropertyResourceValue(CoordConf.hasParameterValue).getLocalName();
				
				specificUniquenessResolutionStrategy.put(assertionRes, value);
			}
		}
		
		return specificUniquenessResolutionStrategy;
	}
	
	public Map<Resource, Integer> specificOntReasoningInterval() {
		if (specificOntReasoningInterval == null) {
			specificOntReasoningInterval = new HashMap<Resource, Integer>();
		
			StmtIterator it = controlPolicyRes.listProperties(CoordConf.hasSpecificOntReasoningInterval);
			for (;it.hasNext();) {
				Resource paramSpec = it.next().getResource();
				Resource assertionRes = paramSpec.getPropertyResourceValue(CoordConf.forContextAssertion);
				int value = paramSpec.getProperty(CoordConf.hasParameterValue).getInt();
				
				specificOntReasoningInterval.put(assertionRes, value);
			}
		}
		
		return specificOntReasoningInterval;
	}
	
	public Map<Resource, Integer> specificTTL() {
		if (specificTTL == null) {
			specificTTL = new HashMap<Resource, Integer>();
		
			StmtIterator it = controlPolicyRes.listProperties(CoordConf.hasSpecificTTLSpec);
			for (;it.hasNext();) {
				Resource paramSpec = it.next().getResource();
				Resource assertionRes = paramSpec.getPropertyResourceValue(CoordConf.forContextAssertion);
				int value = paramSpec.getProperty(CoordConf.hasParameterValue).getInt();
				
				specificTTL.put(assertionRes, value);
			}
		}
		
		return specificTTL;
	}
}
