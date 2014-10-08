package org.aimas.ami.cmm.vocabulary;

import org.topbraid.spin.vocabulary.ARG;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class CoordConf {
	/** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
	
	public final static String BASE_URI = "http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/coordconf";
	public final static String NS = BASE_URI + "#";
	
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    // Vocabulary classes
    /////////////////////
    public final static Resource AssertionUpdateMode = m_model.createResource( NS + "AssertionUpdateMode" );
    public final static Resource ControlPolicy = m_model.createResource( NS + "ControlPolicy" );
    
    // Commands
    public final static Resource OperationalCommand = m_model.createResource( NS + "OperationalCommand" );
    public final static Resource AssertionSpecificCommand = m_model.createResource( NS + "AssertionSpecificCommand" );
    public final static Resource StartAssertionCommand = m_model.createResource( NS + "StartAssertionCommand" );
    public final static Resource StartRuleCommand = m_model.createResource( NS + "StartRuleCommand" );
    public final static Resource StopAssertionCommand = m_model.createResource( NS + "StopAssertionCommand" );
    public final static Resource StopRuleCommand = m_model.createResource( NS + "StopRuleCommand" );
    public final static Resource UpdateModeCommand = m_model.createResource( NS + "UpdateModeCommand" );
    public final static Resource InferenceSchedulingCommand = m_model.createResource( NS + "InferenceSchedulingCommand" );
    public final static Resource QuerySchedulingCommand = m_model.createResource( NS + "QuerySchedulingCommand" );
    
    // Parameters
    public final static Resource OperationalParameter = m_model.createResource( NS + "OperationalParameter" );
    public final static Resource AssertionSpecificParameter = m_model.createResource( NS + "AssertionSpecificParameter" );
    public final static Resource AssertionSpecificConstraintResolutionType = m_model.createResource( NS + "AssertionSpecificConstraintResolutionType" );
    public final static Resource AssertionSpecificEnableSpec = m_model.createResource( NS + "AssertionSpecificEnableSpec" );
    public final static Resource AssertionSpecificOntReasoningInterval = m_model.createResource( NS + "AssertionSpecificOntReasoningInterval" );
    public final static Resource AssertionSpecificRunWindow = m_model.createResource( NS + "AssertionSpecificRunWindow" );
    public final static Resource AssertionSpecificTTLSpec = m_model.createResource( NS + "AssertionSpecificTTLSpec" );
    public final static Resource GeneralParameter = m_model.createResource( NS + "GeneralParameter" );
    public final static Resource OperationalParameterValue = m_model.createResource( NS + "OperationalParameterValue" );
    public final static Resource ConstraintResolutionType = m_model.createResource( NS + "ConstraintResolutionType" );
    public final static Resource SchedulingType = m_model.createResource( NS + "SchedulingType" );
    
    // Statistics
    public final static Resource OperationalStatistic = m_model.createResource( NS + "OperationalStatistic" );
    public final static Resource AssertionSpecificStatistic = m_model.createResource( NS + "AssertionSpecificStatistic" );
    public final static Resource GeneralStatistic = m_model.createResource( NS + "GeneralStatistic" );
    public final static Resource LastDerivation = m_model.createResource( NS + "LastDerivation" );
    
    
    // Vocabulary properties
  	////////////////////////
    public final static Property hasSpecificAssertionEnabling = m_model.createProperty( NS + "hasSpecificAssertionEnabling" );
    public final static Property hasSpecificOntReasoningInterval = m_model.createProperty( NS + "hasSpecificOntReasoningInterval" );
    public final static Property hasSpecificTTLSpec = m_model.createProperty( NS + "hasSpecificTTLSpec" );
    public final static Property enablesAssertionByDefault = m_model.createProperty( NS + "enablesAssertionByDefault" );
    public final static Property forContextAssertion = m_model.createProperty( NS + "forContextAssertion" );
    
    public final static Property hasDefaultOntReasoningInterval = m_model.createProperty( NS + "hasDefaultOntReasoningInterval" );
    public final static Property hasDefaultRunWindow = m_model.createProperty( NS + "hasDefaultRunWindow" );
    public final static Property hasDefaultTTLSpec = m_model.createProperty( NS + "hasDefaultTTLSpec" );
    public final static Property hasInferenceSchedulingType = m_model.createProperty( NS + "hasInferenceSchedulingType" );
    
    public final static Property hasDefaultIntegrityConstraintResolution = m_model.createProperty( NS + "hasDefaultIntegrityConstraintResolution" );
    public final static Property hasDefaultUniquenessConstraintResolution = m_model.createProperty( NS + "hasDefaultUniquenessConstraintResolution" );
    public final static Property hasSpecificUniquenessConstraintResolution = m_model.createProperty( NS + "hasSpecificUniquenessConstraintResolution" );
    public final static Property hasSpecificIntegrityConstraintResolution = m_model.createProperty( NS + "hasSpecificIntegrityConstraintResolution" );
    public final static Property hasSpecificValueConstraintResolution = m_model.createProperty( NS + "hasSpecificValueConstraintResolution" );
    
    public final static Property hasParameterValue = m_model.createProperty( NS + "hasParameterValue" );
    public final static Property hasAcquisitionType = m_model.createProperty( NS + "hasAcquisitionType" );
    public final static Property isDerivedAssertion = m_model.createProperty( NS + "isDerivedAssertion" );
    public final static Property isEnabledAssertion = m_model.createProperty( NS + "isEnabledAssertion" );
    
    public final static Property nrDerivations = m_model.createProperty( NS + "nrDerivations" );
    public final static Property nrQueries = m_model.createProperty( NS + "nrQueries" );
    public final static Property nrSuccessfulDerivations = m_model.createProperty( NS + "nrSuccessfulDerivations" );
    public final static Property nrSuccessfulQueries = m_model.createProperty( NS + "nrSuccessfulQueries" );
    public final static Property setsSchedulingType = m_model.createProperty( NS + "setsSchedulingType" );
    public final static Property setsTransferRate = m_model.createProperty( NS + "setsTransferRate" );
    public final static Property setsUpdateMode = m_model.createProperty( NS + "setsUpdateMode" );
    public final static Property timeSinceLastQuery = m_model.createProperty( NS + "timeSinceLastQuery" );
    
    public final static Property contextAssertionArg = m_model.createProperty( ARG.NS + "contextAssertion" );
    public final static Property elapsedTimeThresholdArg = m_model.createProperty( ARG.NS + "elapsedTimeThreshold" );
    public final static Property intervalEndArg = m_model.createProperty( ARG.NS + "intervalEnd" );
    public final static Property intervalStartArg = m_model.createProperty( ARG.NS + "intervalStart" );
    public final static Property transferRateArg = m_model.createProperty( ARG.NS + "transferRate" );
    public final static Property updateModeArg = m_model.createProperty( ARG.NS + "updateMode" );
    
    // commandRule
    public final static Property hasCommandRule = m_model.createProperty( NS + "hasCommandRule" );
    
    
    // Vocabulary Individuals
  	/////////////////////////
    public final static Resource timeBased = m_model.createResource( NS + "time-based", AssertionUpdateMode);
    public final static Resource changeBased = m_model.createResource( NS + "change-based", AssertionUpdateMode);
    public final static Resource PreferAccurate = m_model.createResource( NS + "PreferAccurate", ConstraintResolutionType);
    public final static Resource PreferNewest = m_model.createResource( NS + "PreferNewest", ConstraintResolutionType);
    public final static Resource DropAll = m_model.createResource( NS + "DropAll", ConstraintResolutionType);
    public final static Resource FCFS = m_model.createResource( NS + "FCFS", SchedulingType);
    public final static Resource UsagePriority = m_model.createResource( NS + "UsagePriority", SchedulingType);
}
