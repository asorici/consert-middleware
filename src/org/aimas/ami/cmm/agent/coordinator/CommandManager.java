package org.aimas.ami.cmm.agent.coordinator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aimas.ami.cmm.agent.config.AgentPolicy;
import org.aimas.ami.cmm.agent.config.CoordinatorSpecification;
import org.aimas.ami.cmm.api.ApplicationControlAdaptor;
import org.aimas.ami.cmm.exceptions.CMMConfigException;
import org.aimas.ami.contextrep.engine.api.CommandHandler;
import org.aimas.ami.contextrep.engine.api.StatsHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

public class CommandManager implements ApplicationControlAdaptor {
	private CtxCoord coordAgent;
	
	private StatsHandler engineStatsAdaptor;
	private CommandHandler engineCommandAdaptor;
	
	private ControlParameters controlParameters;
	private CommandRuleIndex commandRuleIndex;
	
	public ControlParameters getControlParameters() {
		return controlParameters;
	}
	
	public CommandHandler getEngineCommandAdaptor() {
		return engineCommandAdaptor;
	}
	
	public StatsHandler getEngineStatsAdaptor() {
		return engineStatsAdaptor;
	}
	
	// SETUP
	////////////////////////////////////////////////////////////////////////////////////
	public CommandManager(CtxCoord coordAgent, CoordinatorSpecification specification) throws CMMConfigException {
		this.coordAgent = coordAgent;
		setup(specification);
	}

	private void connectToCONSERTEngine() throws CMMConfigException {
		BundleContext context = coordAgent.getOSGiBridge().getBundleContext();
		
		// Get stats handler
		ServiceReference<StatsHandler> statsHandlerRef = context.getServiceReference(StatsHandler.class);
		if (statsHandlerRef == null) {
			throw new CMMConfigException("No reference found for CONSERT Engine service: " + StatsHandler.class.getName());
		}
		engineStatsAdaptor = context.getService(statsHandlerRef);
		
		// Get command handler
		ServiceReference<CommandHandler> commandHandlerRef = context.getServiceReference(CommandHandler.class);
		if (commandHandlerRef == null) {
			throw new CMMConfigException("No reference found for CONSERT Engine service: " + CommandHandler.class.getName());
		}
		engineCommandAdaptor = context.getService(commandHandlerRef);
    }
	
	
	private void setup(CoordinatorSpecification specification) throws CMMConfigException {
		// First retrieve the Coordinator Policy and setup all control parameters
		AgentPolicy controlPolicy = specification.getControlPolicy();
		OntModel controlModel = coordAgent.getConfigurationLoader().load(controlPolicy.getFileNameOrURI());
		controlParameters = new ControlParameters(controlModel);
				
		// Retrieve the CONSERT Engine stats and command adaptors
		connectToCONSERTEngine();
		
		// Configure CONSERT Engine
		configureCONSERTEngine();
		
		// Build the Command Rule Index
		commandRuleIndex = new CommandRuleIndex(this, controlModel);
	}
	
	
	private void configureCONSERTEngine() {
		// STEP 1a: configure CONSERT Engine defaults
		boolean updateEnabledDefault = controlParameters.defaultUpdateEnabled();
		engineCommandAdaptor.setAssertionInsertActiveByDefault(updateEnabledDefault);
		engineCommandAdaptor.setAssertionInferenceActiveByDefault(updateEnabledDefault);
		
		long defaultRunWindow = controlParameters.defaultRunWindow();
		engineCommandAdaptor.setDefaultQueryRunWindow(defaultRunWindow);
		engineCommandAdaptor.setDefaultInferenceRunWindow(defaultRunWindow);
		
		// STEP 1b: configure CONSERT Engine specifics
		Map<Resource, Boolean> specificUpdateEnabled = controlParameters.specificUpdateEnabled();
		for (Resource assertionRes : specificUpdateEnabled.keySet()) {
			engineCommandAdaptor.setAssertionActive(assertionRes, specificUpdateEnabled.get(assertionRes));
		}
    }
	
	// COMMAND EXECUTION
	////////////////////////////////////////////////////////////////////////////////////
	private Map<Resource, Boolean> assertionChangedTracker = new ConcurrentHashMap<Resource, Boolean>(); 
	
	void notifyAssertionUpdated(Resource assertionResource) {
		assertionChangedTracker.put(assertionResource, true);
	}
}
