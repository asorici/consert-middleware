package org.aimas.ami.cmm.resources.sensing;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aimas.ami.cmm.api.ContextAssertionAdaptor;
import org.aimas.ami.contextrep.datatype.CalendarInterval;
import org.aimas.ami.contextrep.datatype.CalendarIntervalList;
import org.aimas.ami.contextrep.model.ContextAssertion.ContextAssertionType;
import org.aimas.ami.contextrep.utils.ContextModelUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.modify.request.QuadDataAcc;
import com.hp.hpl.jena.sparql.modify.request.UpdateCreate;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataInsert;
import com.hp.hpl.jena.update.Update;


public class SenseTemperatureAdaptor extends SensorAdaptorBase {
	
	private Map<String, Integer> temperatureMap;
	
	protected SenseTemperatureAdaptor() {
	    super(SmartClassroom.sensesTemperature.getURI());
	    
	    temperatureMap = new HashMap<String, Integer>();
    }
	
	////////////////////////////////////////////////////////////////////////////////
	@Override
    public boolean supportsChangeBasedUpdateMode() {
	    return true;
    }

	@Override
    public int getMaxUpdateRate() {
	    return 1;
    }

	@Override
    public boolean setUpdateEnabled(boolean updatesEnabled) {
		this.updatesEnabled = updatesEnabled;
		
		return true;
    }

	@Override
    public boolean setUpdateMode(String updateMode, int updateRate) {
		if (updateMode.equals(ContextAssertionAdaptor.TIME_BASED)) {
			this.updateMode = updateMode;
			this.updateRate = updateRate;
			
			return true;
		}
		
		return false;
    }
	
	//////////////////////////////////////////////////////////////////////////////////
	@Override
    protected Map<String, String> getSensorInstanceMap() {
		Map<String, String> instances = new HashMap<String, String>();
		
		instances.put(SmartClassroom.Temp_EF210_Section1_Left.getURI(), SmartClassroom.TemperatureSensor.getURI());
		instances.put(SmartClassroom.Temp_EF210_Section1_Right.getURI(), SmartClassroom.TemperatureSensor.getURI());
		instances.put(SmartClassroom.Temp_EF210_Section3_Left.getURI(), SmartClassroom.TemperatureSensor.getURI());
		instances.put(SmartClassroom.Temp_EF210_Section3_Right.getURI(), SmartClassroom.TemperatureSensor.getURI());
		
		return instances;
    }
	
	@Override
    protected List<Map<Integer, Update>> deliverAssertionUpdates(String sensorIdURI) {
		List<Map<Integer, Update>> updates = new LinkedList<Map<Integer,Update>>();
		SensorInstance sensorInstance = sensorInstances.get(sensorIdURI);
		
		Map<Integer, Update> assertionUpdate = new HashMap<Integer, Update>();
		updates.add(assertionUpdate);
		
		// ======== STEP 1: ASSERTION UUID CREATE
		Node assertionUUIDNode = Node.createURI(ContextModelUtils.createUUID(SmartClassroom.sensesTemperature));
		assertionUpdate.put(ContextAssertionAdaptor.ASSERTION_ID_CREATE, new UpdateCreate(assertionUUIDNode));
		
		// ======== STEP 2: ASSERTION CONTENT
		int temperatureLevel = temperatureMap.get(sensorIdURI);
		
		QuadDataAcc assertionContent = new QuadDataAcc();
		assertionContent.addQuad(Quad.create(assertionUUIDNode, sensorInstance.getIdResource().asNode(), 
			SmartClassroom.sensesTemperature.asNode(), ResourceFactory.createTypedLiteral(new Integer(temperatureLevel)).asNode()));
		assertionUpdate.put(ContextAssertionAdaptor.ASSERTION_CONTENT_UPDATE, new UpdateDataInsert(assertionContent));
		
		// ======== STEP 3: ASSERTION ANNOTATIONS
		Calendar now = now();
		CalendarIntervalList validityIntervals = new CalendarIntervalList();
		
		if (updateMode.equals(ContextAssertionAdaptor.TIME_BASED)) {
			Calendar validityLimit = (Calendar)now.clone();
			validityLimit.add(Calendar.SECOND, updateRate);
			
			validityIntervals.add(new CalendarInterval(now, true, validityLimit, true));
		}
		else {
			validityIntervals.add(new CalendarInterval(now, true, null, false));
		}
		
		List<Statement> assertionAnnotations = ContextModelUtils.createAnnotationStatements(
				assertionUUIDNode.getURI(), SmartClassroom.sensesTemperature.getURI(), 
				ContextAssertionType.Sensed, now, validityIntervals, 1.0, sensorInstance.getIdResource().getURI());
		
		QuadDataAcc annotationContent = new QuadDataAcc();
		String assertionStoreURI = ContextModelUtils.getAssertionStoreURI(SmartClassroom.sensesTemperature.getURI());
		Node assertionStoreNode = Node.createURI(assertionStoreURI);
		
		for (Statement s : assertionAnnotations) {
			annotationContent.addQuad(Quad.create(assertionStoreNode, s.asTriple()));
		}
		
		assertionUpdate.put(ContextAssertionAdaptor.ASSERTION_ANNOTATION_UPDATE, new UpdateDataInsert(annotationContent));
		
		return updates;
    }
}
