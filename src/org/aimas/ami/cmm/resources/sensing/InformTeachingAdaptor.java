package org.aimas.ami.cmm.resources.sensing;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.aimas.ami.cmm.api.ContextAssertionAdaptor;
import org.aimas.ami.contextrep.datatype.CalendarInterval;
import org.aimas.ami.contextrep.datatype.CalendarIntervalList;
import org.aimas.ami.contextrep.model.ContextAssertion.ContextAssertionType;
import org.aimas.ami.contextrep.utils.ContextModelUtils;
import org.aimas.ami.contextrep.vocabulary.ConsertCore;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.modify.request.QuadDataAcc;
import com.hp.hpl.jena.sparql.modify.request.UpdateCreate;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataInsert;
import com.hp.hpl.jena.update.Update;

public class InformTeachingAdaptor extends SensorAdaptorBase {
	
	protected InformTeachingAdaptor() {
	    super(SmartClassroom.takesPlaceIn.getURI());
    }

	private static List<CalendarInterval> getTeachingDates() {
		List<CalendarInterval> teachingDates = new LinkedList<CalendarInterval>();
		
		Calendar mark = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		
		// Interval 1
		Calendar start1 = (Calendar)mark.clone();
		start1.set(Calendar.HOUR_OF_DAY, 10);
		start1.set(Calendar.MINUTE, 0);
		start1.set(Calendar.SECOND, 0);
		start1.set(Calendar.MILLISECOND, 0);
		
		Calendar end1 = (Calendar)mark.clone();
		end1.set(Calendar.HOUR_OF_DAY, 12);
		end1.set(Calendar.MINUTE, 0);
		end1.set(Calendar.SECOND, 0);
		end1.set(Calendar.MILLISECOND, 0);
		
		CalendarInterval interval1 = new CalendarInterval(start1, true, end1, true);
		teachingDates.add(interval1);
		
		// Interval 2
		Calendar start2 = (Calendar)mark.clone();
		start2.set(Calendar.HOUR_OF_DAY, 16);
		start2.set(Calendar.MINUTE, 0);
		start2.set(Calendar.SECOND, 0);
		start2.set(Calendar.MILLISECOND, 0);
		
		Calendar end2 = (Calendar)mark.clone();
		end2.set(Calendar.HOUR_OF_DAY, 18);
		end2.set(Calendar.MINUTE, 0);
		end2.set(Calendar.SECOND, 0);
		end2.set(Calendar.MILLISECOND, 0);
		
		CalendarInterval interval2 = new CalendarInterval(start2, true, end2, true);
		teachingDates.add(interval2);
		
		return teachingDates;
	}
	
	/* Content Management */
	private Resource teachingActivityRes;
	
	private CalendarInterval getCurrentTeachingInterval() {
	    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	    List<CalendarInterval> teachingDates = getTeachingDates();
	    
	    for (CalendarInterval interval : teachingDates) {
	    	if (interval.includes(now)) {
	    		return interval;
	    	}
	    }
	    
	    return null;
    }
	
	private Model createTeachingActivityInstance(CalendarInterval teachingInterval) {
	    // create the activity teaching model, which will hold all the statements that must be given as an EntityStore update
	    Model teachingActivityModel = ModelFactory.createDefaultModel();
	    
		String instanceURI = SensingUtil.generateUniqueURI(SmartClassroom.TeachingActivity.getURI());
	    teachingActivityRes = teachingActivityModel.createResource(instanceURI, SmartClassroom.TeachingActivity);
		
	    // create the start and end InstantThing instances for the activity interval
	    Resource intervalStartRes = teachingActivityModel.createResource(
	    		SensingUtil.generateUniqueURI(SmartClassroom.InstantThing.getURI()), SmartClassroom.InstantThing);
	    Resource intervalEndRes = teachingActivityModel.createResource(
	    		SensingUtil.generateUniqueURI(SmartClassroom.InstantThing.getURI()), SmartClassroom.InstantThing);
	    
	    intervalStartRes.addProperty(SmartClassroom.at, teachingActivityModel.createTypedLiteral(teachingInterval.lowerLimit()));
	    intervalEndRes.addProperty(SmartClassroom.at, teachingActivityModel.createTypedLiteral(teachingInterval.upperLimit()));
	    
	    teachingActivityRes.addProperty(SmartClassroom.from, intervalStartRes);
	    teachingActivityRes.addProperty(SmartClassroom.to, intervalEndRes);
	    
	    return teachingActivityModel;
    }
	
	
	////////////////////////////////////////////////////////////////////////////////////
	@Override
    public boolean supportsChangeBasedUpdateMode() {
	    return true;
    }

	@Override
    public int getMaxUpdateRate() {
	    return 0;
    }
	
	@Override
    public boolean setUpdateEnabled(boolean updatesEnabled) {
		this.updatesEnabled = updatesEnabled;
		
		return true;
    }

	@Override
    public boolean setUpdateMode(String updateMode, int updateRate) {
		this.updateMode = updateMode;
		this.updateRate = updateRate;
		
		return true;
    }
	
	////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected Map<String, String> getSensorInstanceMap() {
		Map<String, String> instances = new HashMap<String, String>();
		
		instances.put(SmartClassroom.TeachingActivitySensor.getURI(), ConsertCore.CONTEXT_AGENT.getURI());
		
		return instances;
	}
	
	@Override
    protected SensorInstance deliverSensorEntityInformation(String sensorIdURI, String sensorTypeURI) {
		SensorInstance sensorInstance = super.deliverSensorEntityInformation(sensorIdURI, sensorTypeURI);
		
		Resource instanceRes = sensorInstance.getIdResource();
		instanceRes.addProperty(ConsertCore.CONTEXT_AGENT_TYPE_PROPERTY, ConsertCore.CTX_SENSOR);
		
		return sensorInstance;
    }
	
	
	@Override
    protected List<Map<Integer, Update>> deliverAssertionUpdates(String sensorIdURI) {
		List<Map<Integer, Update>> updates = new LinkedList<Map<Integer,Update>>();
		CalendarInterval teachingInterval = getCurrentTeachingInterval();
		
		Map<Integer, Update> assertionUpdate = new HashMap<Integer, Update>();
		updates.add(assertionUpdate);
		
	    if (teachingInterval != null) {
			// ======== STEP 1 (optional): ENTITY STORE UPDATE
			if (teachingActivityRes == null) {
				QuadDataAcc entityStoreData = new QuadDataAcc();
				Node entityStoreNode = Node.createURI(ConsertCore.ENTITY_STORE_URI);
				
		    	// We must introduce the created activity in the EntityStore, so we must create an update request
		    	// which includes updating the EntityStore
		    	Model teachingActivityModel = createTeachingActivityInstance(teachingInterval);
		    	
		    	StmtIterator it = teachingActivityModel.listStatements();
		    	for (;it.hasNext();) {
		    		Statement s = it.next();
		    		entityStoreData.addQuad(Quad.create(entityStoreNode, s.asTriple()));
		    	}
		    	
		    	assertionUpdate.put(ContextAssertionAdaptor.ASSERTION_ENTITY_UPDATE, new UpdateDataInsert(entityStoreData));
			}
			
			// ======== STEP 2: ASSERTION UUID CREATE
			Node assertionUUIDNode = Node.createURI(ContextModelUtils.createUUID(SmartClassroom.takesPlaceIn));
			assertionUpdate.put(ContextAssertionAdaptor.ASSERTION_ID_CREATE, new UpdateCreate(assertionUUIDNode));
			
			// ======== STEP 3: ASSERTION CONTENT
			QuadDataAcc assertionContent = new QuadDataAcc();
			assertionContent.addQuad(Quad.create(assertionUUIDNode, teachingActivityRes.asNode(), 
					SmartClassroom.takesPlaceIn.asNode(), SmartClassroom.EF210_Room.asNode()));
			assertionUpdate.put(ContextAssertionAdaptor.ASSERTION_CONTENT_UPDATE, new UpdateDataInsert(assertionContent));
			
			// ======== STEP 4: ASSERTION ANNOTATIONS
			Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			CalendarIntervalList validityIntervals = new CalendarIntervalList();
			validityIntervals.add(teachingInterval);
			
			SensorInstance sensorInstance = sensorInstances.get(sensorIdURI);
			List<Statement> assertionAnnotations = ContextModelUtils.createAnnotationStatements(
					assertionUUIDNode.getURI(), SmartClassroom.takesPlaceIn.getURI(), 
					ContextAssertionType.Profiled, now, validityIntervals, 1.0, sensorInstance.getIdResource().getURI());
			
			QuadDataAcc annotationContent = new QuadDataAcc();
			String assertionStoreURI = ContextModelUtils.getAssertionStoreURI(SmartClassroom.takesPlaceIn.getURI());
			Node assertionStoreNode = Node.createURI(assertionStoreURI);
			
			for (Statement s : assertionAnnotations) {
				annotationContent.addQuad(Quad.create(assertionStoreNode, s.asTriple()));
			}
			
			assertionUpdate.put(ContextAssertionAdaptor.ASSERTION_ANNOTATION_UPDATE, new UpdateDataInsert(annotationContent));
		}
	    
	    return updates;
    }
}
