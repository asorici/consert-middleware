package org.aimas.ami.cmm.utils;

import java.io.IOException;
import java.io.InputStream;

import org.aimas.ami.cmm.api.CMMConfigException;
import org.aimas.ami.contextrep.utils.ResourceManager;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.util.Locator;


public class AgentConfigLoader {
	public static final String CMM_RESOURCE_PATH = "etc/cmm/";
	public static final String CMM_ONTOLOGIES_PATH = CMM_RESOURCE_PATH + "ontologies/";
	public static final String CMM_PROPERTIES_FILE = CMM_RESOURCE_PATH + "cmm.properties";
	
	public static final String CMM_DOCMGR_FILE = CMM_RESOURCE_PATH + "cmm-ont-policy.rdf";
	public static final String CMM_AGENT_CONFIG_FILE_TTL = CMM_RESOURCE_PATH + "agent-config.ttl";
	public static final String CMM_AGENT_CONFIG_FILE_RDF = CMM_RESOURCE_PATH + "agent-config.rdf";
	
	private ResourceManager cmmResourceManager;
	
	private OntDocumentManager cmmDocManager;
	
	public AgentConfigLoader(ResourceManager resourceManager) throws CMMConfigException {
		this.cmmResourceManager = resourceManager;
		configure();
	}
	
	public ResourceManager getResourceManager() {
		return cmmResourceManager;
	}
	
	private void configure() throws CMMConfigException {
		// STEP 1: validate resource manager
		validate();
		
		// STEP 2: setup document manager
		cmmDocManager = setupDocumentManager();
	}
	
	private void validate() throws CMMConfigException {
		// STEP 1: try to locate CMM document manager file
		InputStream cmmDocMgrStream = cmmResourceManager.getResourceAsStream(CMM_DOCMGR_FILE);
		if (cmmDocMgrStream == null) {
			throw new CMMConfigException("CMM document manager file " + CMM_DOCMGR_FILE + " not found in resources.");
		}
		
		// STEP 2: try to locate CMM configuration file: first as TTL file
		InputStream cmmConfStream = cmmResourceManager.getResourceAsStream(CMM_AGENT_CONFIG_FILE_TTL);
		if (cmmConfStream == null) {
			// then as rdf file
			cmmConfStream = cmmResourceManager.getResourceAsStream(CMM_AGENT_CONFIG_FILE_RDF);
			
			if (cmmConfStream == null) { 
				throw new CMMConfigException("Neither .ttl, nor .rdf versions of the CMM configuration file " + "etc/cmm/agent-config" + " were found in resources.");
			}
		}
	}
	
	
	
	private OntDocumentManager setupDocumentManager() throws CMMConfigException {
		Locator cmmLocator = cmmResourceManager.getResourceLocator();
		
		// ======== setup the cmm-global document manager  ========
        try {
			Model cmmDocMgrModel = ModelFactory.createDefaultModel();
	        InputStream cmmDocMgrStream = cmmResourceManager.getResourceAsStream(CMM_DOCMGR_FILE); 
	        
	        cmmDocMgrModel.read(cmmDocMgrStream, null);
	        cmmDocMgrStream.close();
	        
	        OntDocumentManager cmmDocManager = new OntDocumentManager();
	        cmmDocManager.configure(cmmDocMgrModel);
	        cmmDocManager.getFileManager().addLocator(cmmLocator);
	        
	        return cmmDocManager;
        } 
        catch(IOException e) {
        	throw new CMMConfigException("Error reading CMM document manager file " + CMM_DOCMGR_FILE, e); 
        }
    }
	
	
	public OntModel loadAgentConfiguration() throws CMMConfigException {
		// We know it has to be one or the other because we passed the configure()
		if (cmmResourceManager.hasResource(CMM_AGENT_CONFIG_FILE_TTL)) {
			return load(CMM_AGENT_CONFIG_FILE_TTL);
		}
		else {
			return load(CMM_AGENT_CONFIG_FILE_RDF);
		}
	}
	
	
	public OntModel load(String filenameOrURI) throws CMMConfigException {
		try {
			// create the OntModelSpec
			OntModelSpec agentConfModelSpec = new OntModelSpec(OntModelSpec.OWL_MEM);
			agentConfModelSpec.setDocumentManager(cmmDocManager);
			
			// create the agent configuration OntModel
			OntModel agentConfModel = ModelFactory.createOntologyModel(agentConfModelSpec);
			agentConfModel.add(cmmDocManager.getFileManager().loadModel(filenameOrURI));
			cmmDocManager.loadImports(agentConfModel);
			
			return agentConfModel;
		} 
		catch(NotFoundException e) {
			throw new CMMConfigException("The filename or URI " + filenameOrURI + 
					" could not be found within the resources managed by this loader.", e);
		}
		catch(JenaException e) {
			throw new CMMConfigException("Syntax error for model loaded from filename or URI " + filenameOrURI, e);
		}
	}
	
	
	public OntModel load(Resource uriResource) throws CMMConfigException {
		return load(uriResource.getURI());
	}
	
	
	public Model loadDocumentModel(String filename) throws CMMConfigException {
		try {
			Model m = ModelFactory.createDefaultModel();
			
			// do simple file type check since we know we only server XML or TTL based RDF files
			String serializationLang = "RDF/XML";
			if (filename.endsWith(".ttl")) {
				serializationLang = "TTL";
			}
			
			InputStream documentInput = cmmResourceManager.getResourceAsStream(filename);
			m.read(documentInput, null, serializationLang);
			
			return m;
		} 
		catch(NotFoundException e) {
			throw new CMMConfigException("The filename or URI " + filename + 
					" could not be found within the resources managed by this loader.", e);
		}
		catch(JenaException e) {
			throw new CMMConfigException("Syntax error for model loaded from filename or URI " + filename, e);
		}
	}
}
