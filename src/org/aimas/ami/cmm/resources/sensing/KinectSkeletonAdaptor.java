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
import org.aimas.ami.contextrep.vocabulary.ConsertCore;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.modify.request.QuadDataAcc;
import com.hp.hpl.jena.sparql.modify.request.UpdateCreate;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataInsert;
import com.hp.hpl.jena.update.Update;
import com.hp.hpl.jena.vocabulary.RDF;


public class KinectSkeletonAdaptor extends SensorAdaptorBase {
	private List<SkeletonInfo> skeletonTracker;
	
	protected KinectSkeletonAdaptor() {
	    super(SmartClassroom.sensesSkeletonInPosition.getURI());
	    
	    this.skeletonTracker = new LinkedList<SkeletonInfo>();
    }
	
	@Override
    public boolean supportsChangeBasedUpdateMode() {
	    return false;
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
	
	/////////////////////////////////////////////////////////////////////////
	@Override
    protected Map<String, String> getSensorInstanceMap() {
		Map<String, String> instances = new HashMap<String, String>();
		
		instances.put(SmartClassroom.Kinect_EF210_PresenterArea.getURI(), SmartClassroom.KinectCamera.getURI());
		instances.put(SmartClassroom.Kinect_EF210_Section1_Left.getURI(), SmartClassroom.KinectCamera.getURI());
		instances.put(SmartClassroom.Kinect_EF210_Section1_Right.getURI(), SmartClassroom.KinectCamera.getURI());
		instances.put(SmartClassroom.Kinect_EF210_Section2_Left.getURI(), SmartClassroom.KinectCamera.getURI());
		instances.put(SmartClassroom.Kinect_EF210_Section2_Right.getURI(), SmartClassroom.KinectCamera.getURI());
		instances.put(SmartClassroom.Kinect_EF210_Section3_Left.getURI(), SmartClassroom.KinectCamera.getURI());
		instances.put(SmartClassroom.Kinect_EF210_Section3_Right.getURI(), SmartClassroom.KinectCamera.getURI());
		
		return instances;
    }
	
	@Override
    protected List<Map<Integer, Update>> deliverAssertionUpdates(String sensorIdURI) {
		List<Map<Integer, Update>> updates = new LinkedList<Map<Integer,Update>>();
		SensorInstance sensorInstance = sensorInstances.get(sensorIdURI);
		
		for (SkeletonInfo skelInfo : skeletonTracker) {
			Map<Integer, Update> assertionUpdate = new HashMap<Integer, Update>();
			
			Model skeletonModel = ModelFactory.createDefaultModel();
			Resource skeletonRes = skeletonModel.createResource(skelInfo.getSkeletonIdURI(), SmartClassroom.KinectSkeleton);
			Resource skelPositionRes = skeletonModel.createResource(skelInfo.getSkeletonPosition(), SmartClassroom.SkeletonPosition);
		
			// ======== STEP 1
			QuadDataAcc entityStoreData = new QuadDataAcc();
			Node entityStoreNode = Node.createURI(ConsertCore.ENTITY_STORE_URI);
	    	
	    	StmtIterator it = skeletonModel.listStatements();
	    	for (;it.hasNext();) {
	    		Statement s = it.next();
	    		entityStoreData.addQuad(Quad.create(entityStoreNode, s.asTriple()));
	    	}
	    	
	    	assertionUpdate.put(ContextAssertionAdaptor.ASSERTION_ENTITY_UPDATE, new UpdateDataInsert(entityStoreData));
	    	
	    	// ======== STEP 2: ASSERTION UUID CREATE
	    	Node assertionUUIDNode = Node.createURI(ContextModelUtils.createUUID(SmartClassroom.sensesSkeletonInPosition));
	    	assertionUpdate.put(ContextAssertionAdaptor.ASSERTION_ID_CREATE, new UpdateCreate(assertionUUIDNode));
	    	
	    	// ======== STEP 3: ASSERTION CONTENT
	    	QuadDataAcc assertionContent = new QuadDataAcc();
	    	Resource bnode = ResourceFactory.createResource();
	    	assertionContent.addQuad(Quad.create(assertionUUIDNode, bnode.asNode(), 
		    	RDF.type.asNode(), SmartClassroom.sensesSkeletonInPosition.asNode()));
	    	assertionContent.addQuad(Quad.create(assertionUUIDNode, bnode.asNode(), 
			    SmartClassroom.hasCameraRole.asNode(), sensorInstance.getIdResource().asNode()));
	    	assertionContent.addQuad(Quad.create(assertionUUIDNode, bnode.asNode(), 
			    SmartClassroom.hasSkeletonRole.asNode(), skeletonRes.asNode()));
	    	assertionContent.addQuad(Quad.create(assertionUUIDNode, bnode.asNode(), 
				SmartClassroom.hasSkelPositionRole.asNode(), skelPositionRes.asNode()));
		    	
	    	assertionUpdate.put(ContextAssertionAdaptor.ASSERTION_CONTENT_UPDATE, new UpdateDataInsert(assertionContent));
	    	
	    	// ======== STEP 4: ASSERTION ANNOTATIONS
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
					assertionUUIDNode.getURI(), SmartClassroom.sensesSkeletonInPosition.getURI(), 
					ContextAssertionType.Sensed, now, validityIntervals, 1.0, sensorInstance.getIdResource().getURI());
			
			QuadDataAcc annotationContent = new QuadDataAcc();
			String assertionStoreURI = ContextModelUtils.getAssertionStoreURI(SmartClassroom.sensesSkeletonInPosition.getURI());
			Node assertionStoreNode = Node.createURI(assertionStoreURI);
			
			for (Statement s : assertionAnnotations) {
				annotationContent.addQuad(Quad.create(assertionStoreNode, s.asTriple()));
			}
			
			assertionUpdate.put(ContextAssertionAdaptor.ASSERTION_ANNOTATION_UPDATE, new UpdateDataInsert(annotationContent));
			
			// ========
			updates.add(assertionUpdate);
		}
		
		return updates;
    }

	/////////////////////////////////////////////////////////////////////////
	private static class SkeletonInfo {
		String skeletonIdURI;
		String skeletonPosition;
		
		public SkeletonInfo(String skeletonIdURI, String skeletonPosition) {
	        this.skeletonIdURI = skeletonIdURI;
	        this.skeletonPosition = skeletonPosition;
        }
		
		public String getSkeletonIdURI() {
			return skeletonIdURI;
		}
		
		public String getSkeletonPosition() {
			return skeletonPosition;
		}
	}
}
