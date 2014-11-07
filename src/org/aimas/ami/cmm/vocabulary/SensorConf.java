package org.aimas.ami.cmm.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class SensorConf {
	/** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
	
	public final static String BASE_URI = "http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/sensorconf";
	public final static String NS = BASE_URI + "#";
	
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    // Vocabulary classes
    /////////////////////
    public final static Resource SensingPolicy = m_model.createResource( NS + "SensingPolicy" );
    //public final static Resource SensorInstance = m_model.createResource( NS + "SensorInstance" );
    
    // Vocabulary properties
   	////////////////////////
    public final static Property hasUpdateMode = m_model.createProperty( NS + "hasUpdateMode" );
    public final static Property hasUpdateRate = m_model.createProperty( NS + "hasUpdateRate" );
    //public final static Property appliesTo = m_model.createProperty( NS + "appliesTo" );
    //public final static Property instanceID = m_model.createProperty( NS + "instanceID" );
    //public final static Property instanceType = m_model.createProperty( NS + "instanceType" );
    
}
