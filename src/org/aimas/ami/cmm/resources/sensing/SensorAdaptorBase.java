package org.aimas.ami.cmm.resources.sensing;

import java.util.List;

import org.aimas.ami.cmm.api.ApplicationSensingAdaptor;
import org.aimas.ami.cmm.api.ContextAssertionAdaptor;
import org.aimas.ami.cmm.api.ContextAssertionDescription;
import org.aimas.ami.contextrep.vocabulary.ConsertCore;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.modify.request.QuadDataAcc;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataInsert;
import com.hp.hpl.jena.update.Update;
import com.hp.hpl.jena.update.UpdateRequest;

public abstract class SensorAdaptorBase implements ContextAssertionAdaptor {
	
	/* Info about this instance */
	protected Resource sensorInstance;
	protected Model sensorInfoModel;
	
	/* ContextAssertion description */
	protected String contextAssertionURI;
	protected ContextAssertionDescription assertionDescription;
	
	/* Update state and management */
	protected ApplicationSensingAdaptor sensingAdaptor;
	protected boolean updatesEnabled = false;
	protected String updateMode = SensingUtil.CHANGE_BASED_UPDATE;
	protected int updateRate = 0;
	
	/* Internal access management */
	protected Object updateRequestSynchronizer = new Object();
	
	
	protected SensorAdaptorBase(String contextAssertionURI) {
		this.contextAssertionURI = contextAssertionURI;
	}
	
	
	@Override
    public void registerSensingAdaptor(ApplicationSensingAdaptor sensingAdaptor) {
	    this.sensingAdaptor = sensingAdaptor;
    }
	
	
	@Override
    public ContextAssertionDescription getProvidedAssertion() {
	    if (assertionDescription == null) {
	    	assertionDescription = new ContextAssertionDescription(SmartClassroom.TeachingActivity.getURI());
	    	assertionDescription.setSupportedAnnotationURIs(SensingUtil.getStandardAnnotations());
	    }
	    
	    return assertionDescription;
    }
	
	@Override
    public void setState(boolean updatesEnabled, String updateMode, int updateRate) {
	    this.updatesEnabled = updatesEnabled;
	    this.updateMode = updateMode;
	    this.updateRate = updateRate;
    }
	
	
	@Override
	public UpdateRequest deliverUpdate(ApplicationSensingAdaptor sensingAdaptor) {
		UpdateRequest updateRequest = new UpdateRequest();
		
		// 1) Check if sensorInfo has been instantiated. If not, we have to include it in the first update
		// as an insertion into the EntityStore
		synchronized(updateRequestSynchronizer) {
			if (sensorInstance == null) {
				sensorInfoModel = ModelFactory.createDefaultModel();
				sensorInstance = deliverSensorEntityInformation(sensorInfoModel);
				
				QuadDataAcc entityStoreData = new QuadDataAcc();
				Node entityStoreNode = Node.createURI(ConsertCore.ENTITY_STORE_URI);
				
				StmtIterator it = sensorInfoModel.listStatements();
		    	for (;it.hasNext();) {
		    		Statement s = it.next();
		    		entityStoreData.addQuad(Quad.create(entityStoreNode, s.asTriple()));
		    	}
				Update sensorEntityUpdate = new UpdateDataInsert(entityStoreData);
				updateRequest.add(sensorEntityUpdate);
			}
		}
		
		// 2) Create the ContextAssertion entity, content and annotation updates
		List<Update> assertionUpdates = deliverAssertionUpdate();
		for (Update u : assertionUpdates) {
			updateRequest.add(u);
		}
		
		if (updateRequest.getOperations().isEmpty()) {
			return null;
		}
		
		return updateRequest;
	}
	
	protected abstract Resource deliverSensorEntityInformation(Model sensorInfoModel);
	
	protected abstract List<Update> deliverAssertionUpdate();
}
