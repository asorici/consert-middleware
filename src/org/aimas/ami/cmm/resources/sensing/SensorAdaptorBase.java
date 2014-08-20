package org.aimas.ami.cmm.resources.sensing;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.aimas.ami.cmm.api.ApplicationSensingAdaptor;
import org.aimas.ami.cmm.api.ContextAssertionAdaptor;
import org.aimas.ami.cmm.api.ContextAssertionDescription;
import org.aimas.ami.contextrep.resources.TimeService;
import org.aimas.ami.contextrep.vocabulary.ConsertCore;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.modify.request.QuadDataAcc;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataInsert;
import com.hp.hpl.jena.update.Update;
import com.hp.hpl.jena.update.UpdateRequest;

public abstract class SensorAdaptorBase implements ContextAssertionAdaptor {
	
	/* Info about the physical sensor instances managed by this adaptor */
	protected Map<String, SensorInstance> sensorInstances;
	protected Map<String, Boolean> sensorInstancePublished;
	
	/* ContextAssertion description */
	protected String contextAssertionURI;
	protected ContextAssertionDescription assertionDescription;
	
	/* Update state and management */
	protected ApplicationSensingAdaptor sensingAdaptor;
	protected boolean updatesEnabled = false;
	protected String updateMode = SensingUtil.CHANGE_BASED_UPDATE;
	protected int updateRate = 0;
	
	/* Time management */
	protected TimeService timeService;
	
	protected SensorAdaptorBase(String contextAssertionURI) {
		this.contextAssertionURI = contextAssertionURI;
		
		setSensorInstanceDescriptions();
	}
	
	private void setSensorInstanceDescriptions() {
		sensorInstances = new HashMap<String, SensorInstance>();
		sensorInstancePublished = new HashMap<String, Boolean>();
		
		Map<String, String> instancesMap = getSensorInstanceMap();
		for (String sensorIdURI : instancesMap.keySet()) {
			String sensorTypeURI = instancesMap.get(sensorIdURI);
			sensorInstances.put(sensorIdURI, deliverSensorEntityInformation(sensorIdURI, sensorTypeURI));
			sensorInstancePublished.put(sensorIdURI, false);
		}
    }
	
	//
	//////////////////////////////////////////////////////////////////////////////
	@Override
    public void registerSensingAdaptor(ApplicationSensingAdaptor sensingAdaptor) {
	    this.sensingAdaptor = sensingAdaptor;
    }
	
	@Override
    public ContextAssertionDescription getProvidedAssertion() {
	    if (assertionDescription == null) {
	    	assertionDescription = new ContextAssertionDescription(contextAssertionURI);
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
	
	//
	///////////////////////////////////////////////////////////////////////////////
	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}
	
	protected Calendar now() {
		if (timeService != null) {
			return timeService.getCalendarInstance();
		}
		
		return Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	}
	
	protected long currentTimeMillis() {
		if (timeService != null) {
			return timeService.getCurrentTimeMillis();
		}
		
		return System.currentTimeMillis();
	}
	
	//
	///////////////////////////////////////////////////////////////////////////////
	/** 
	 * Deliver an update of the ContextAssertion instance given by the physical sensor specified by
	 * <code>sensorIdURI</code>. 
	 * This method is expected to produce an {@link UpdateRequest} that contains the content and annotation update
	 * of the ContextAssertion that this adaptor manages.
	 * @param sensorIdURI
	 */
	public List<UpdateRequest> deliverUpdates(String sensorIdURI) {
		List<UpdateRequest> updateRequests = new LinkedList<UpdateRequest>();
		
		// 1) Check if sensorInfo has been instantiated. If not, we have to include an initial update request
		// that makes an insertion into the EntityStore with the description of this sensor
		if (!sensorInstancePublished.get(sensorIdURI)) {
			SensorInstance sensorInstance = sensorInstances.get(sensorIdURI);
			sensorInstancePublished.put(sensorIdURI, true);
			
			UpdateRequest updateReq = new UpdateRequest();
			
			QuadDataAcc entityStoreData = new QuadDataAcc();
			Node entityStoreNode = Node.createURI(ConsertCore.ENTITY_STORE_URI);
			
			StmtIterator it = sensorInstance.getInfoModel().listStatements();
	    	for (;it.hasNext();) {
	    		Statement s = it.next();
	    		entityStoreData.addQuad(Quad.create(entityStoreNode, s.asTriple()));
	    	}
			Update sensorEntityUpdate = new UpdateDataInsert(entityStoreData);
			updateReq.add(sensorEntityUpdate);
			
			updateRequests.add(updateReq);
		}
		
		
		// 2) Create one or more UpdateRequests containing updates for EntityStore, Assertion content 
		// and Assertion annotation
		List<Map<Integer, Update>> assertionUpdates = deliverAssertionUpdates(sensorIdURI);
		for (Map<Integer, Update> assertionUpdate : assertionUpdates) {
			UpdateRequest updateReq = new UpdateRequest();
			
			Update entityStoreUpdate = assertionUpdate.get(ContextAssertionAdaptor.ASSERTION_ENTITY_UPDATE);
			Update idCreate = assertionUpdate.get(ContextAssertionAdaptor.ASSERTION_ID_CREATE);
			Update contentUpdate = assertionUpdate.get(ContextAssertionAdaptor.ASSERTION_CONTENT_UPDATE);
			Update annotationUpdate = assertionUpdate.get(ContextAssertionAdaptor.ASSERTION_ANNOTATION_UPDATE);
			
			if (entityStoreUpdate != null) {
				updateReq.add(entityStoreUpdate);
			}
			updateReq.add(idCreate);
			updateReq.add(contentUpdate);
			updateReq.add(annotationUpdate);
			
			updateRequests.add(updateReq);
		}
		
		if (updateRequests.isEmpty()) {
			return null;
		}
		
		return updateRequests;
	}
	
	/**
	 * Deliver the sensor instance class that contains the description of a physical (or virtual) sensor
	 * that provides updates for the ContextAssertion managed by this adaptor.
	 * @param sensorIdURI
	 * @param sensorTypeURI
	 * @return The {@link SensorInstance} describing the sensor (the source annotation)
	 */
	protected SensorInstance deliverSensorEntityInformation(String sensorIdURI, String sensorTypeURI) {
		SensorInstance sensorInstance = new SensorInstance(sensorIdURI, sensorTypeURI);
		return sensorInstance;
	}
	
	/**
	 * @return The list of {@link Update} statements (EntityStore updates, Assertion ID creation, 
	 * Assertion content and annotations) that make up a ContextAssertion update request.
	 */
	protected abstract List<Map<Integer, Update>> deliverAssertionUpdates(String sensorIdURI);
	
	
	protected abstract Map<String, String> getSensorInstanceMap();
}
