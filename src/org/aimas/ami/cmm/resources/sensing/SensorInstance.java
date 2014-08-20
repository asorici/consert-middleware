package org.aimas.ami.cmm.resources.sensing;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class SensorInstance {
	private Resource idResource;
	private Model infoModel;
	
	public SensorInstance(String sensorIdURI, String sensorTypeURI) {
	    // create the model
		infoModel = ModelFactory.createDefaultModel();
		
		Resource sensorTypeResource = ResourceFactory.createResource(sensorTypeURI);
		idResource = infoModel.createResource(sensorIdURI, sensorTypeResource);
    }

	public Resource getIdResource() {
		return idResource;
	}
	
	public Model getInfoModel() {
		return infoModel;
	}
}
