package org.aimas.ami.cmm.agent.config;

import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class ContextModelDefinition {
	
	private String modelDocumentManagerFileOrURI;
	private String modelCoreFileOrURI;
	private String modelAnnotationsFileOrURI;
	private String modelConstraintsFileOrURI;
	private String modelFunctionsFileOrURI;
	private String modelRulesFileOrURI;
	
	public ContextModelDefinition(String modelDocumentManagerFileOrURI,
            String modelCoreFileOrURI, String modelAnnotationsFileOrURI,
            String modelConstraintsFileOrURI, String modelFunctionsFileOrURI,
            String modelRulesFileOrURI) {
	    
		this.modelDocumentManagerFileOrURI = modelDocumentManagerFileOrURI;
	    this.modelCoreFileOrURI = modelCoreFileOrURI;
	    this.modelAnnotationsFileOrURI = modelAnnotationsFileOrURI;
	    this.modelConstraintsFileOrURI = modelConstraintsFileOrURI;
	    this.modelFunctionsFileOrURI = modelFunctionsFileOrURI;
	    this.modelRulesFileOrURI = modelRulesFileOrURI;
    }

	public String getModelDocumentManagerFileOrURI() {
		return modelDocumentManagerFileOrURI;
	}

	public String getModelCoreFileOrURI() {
		return modelCoreFileOrURI;
	}

	public String getModelAnnotationsFileOrURI() {
		return modelAnnotationsFileOrURI;
	}

	public String getModelConstraintsFileOrURI() {
		return modelConstraintsFileOrURI;
	}

	public String getModelFunctionsFileOrURI() {
		return modelFunctionsFileOrURI;
	}

	public String getModelRulesFileOrURI() {
		return modelRulesFileOrURI;
	}
	
	public static ContextModelDefinition fromConfigurationModel(OntModel cmmConfigModel, Resource contextModelResource) {
		Resource modelDocumentManagerRes = cmmConfigModel.getProperty(contextModelResource, OrgConf.hasModelDocumentManager).getResource();
		Resource modelCoreRes = cmmConfigModel.getProperty(contextModelResource, OrgConf.hasModelCoreDocument).getResource();
		Resource modelAnnotationRes = cmmConfigModel.getProperty(contextModelResource, OrgConf.hasModelAnnotationsDocument).getResource();
		Resource modelConstraintsRes = cmmConfigModel.getProperty(contextModelResource, OrgConf.hasModelConstraintsDocument).getResource();
		Resource modelFunctionsRes = cmmConfigModel.getProperty(contextModelResource, OrgConf.hasModelFunctionsDocument).getResource();
		Resource modelRulesRes = cmmConfigModel.getProperty(contextModelResource, OrgConf.hasModelRulesDocument).getResource();
		
		String modelDocumentManagerFileOrURI = getFileOrURI(modelDocumentManagerRes);
		String modelCoreFileOrURI = getFileOrURI(modelCoreRes);
		String modelAnnotationsFileOrURI = getFileOrURI(modelAnnotationRes);
		String modelConstraintsFileOrURI = getFileOrURI(modelConstraintsRes);
		String modelFunctionsFileOrURI = getFileOrURI(modelFunctionsRes);
		String modelRulesFileOrURI = getFileOrURI(modelRulesRes);
		
		return new ContextModelDefinition(modelDocumentManagerFileOrURI, modelCoreFileOrURI, 
				modelAnnotationsFileOrURI, modelConstraintsFileOrURI, modelFunctionsFileOrURI, modelRulesFileOrURI);
	}
	
	private static String getFileOrURI(Resource modelDocumentRes) {
		Statement docFileStmt = modelDocumentRes.getProperty(OrgConf.documentPath);
		if (docFileStmt != null) {
			return docFileStmt.getString();
		}
		else {
			docFileStmt = modelDocumentRes.getProperty(OrgConf.documentURI);
			return docFileStmt.getString();
		}
	}
}
