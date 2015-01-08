package org.aimas.ami.cmm.agent.coordinator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.aimas.ami.cmm.agent.config.AgentPolicy;
import org.aimas.ami.cmm.agent.config.CoordinatorSpecification;
import org.aimas.ami.cmm.api.ApplicationControlAdaptor;
import org.aimas.ami.cmm.api.CMMConfigException;
import org.aimas.ami.cmm.vocabulary.CoordConf;
import org.aimas.ami.contextrep.engine.api.CommandHandler;
import org.aimas.ami.contextrep.engine.api.ContextDerivationRule;
import org.aimas.ami.contextrep.engine.api.EngineInferenceStats;
import org.aimas.ami.contextrep.engine.api.EngineQueryStats;
import org.aimas.ami.contextrep.engine.api.StatsHandler;
import org.aimas.ami.contextrep.model.ContextAssertion.ContextAssertionType;
import org.topbraid.spin.arq.ARQFactory;

import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

public class CommandManager implements ApplicationControlAdaptor {
	private CtxCoord coordAgent;
	
	private StatsHandler engineStatsAdaptor;
	private CommandHandler engineCommandAdaptor;
	
	private ControlParameters controlParameters;
	private CommandRuleIndex commandRuleIndex;
	
	public CtxCoord getCoordinatorAgent() {
		return coordAgent;
	}
	
	public ControlParameters getControlParameters() {
		return controlParameters;
	}
	
	public CommandRuleIndex getCommandRuleIndex() {
		return commandRuleIndex;
	}
	
	public CommandHandler getEngineCommandAdaptor() {
		return engineCommandAdaptor;
	}
	
	public StatsHandler getEngineStatsAdaptor() {
		return engineStatsAdaptor;
	}
	
	// SETUP
	////////////////////////////////////////////////////////////////////////////////////
	public CommandManager(CtxCoord coordAgent, CoordinatorSpecification specification, 
			CommandHandler engineCommandAdaptor, StatsHandler engineStatsAdaptor) throws CMMConfigException {
		this.coordAgent = coordAgent;
		this.engineCommandAdaptor = engineCommandAdaptor;
		this.engineStatsAdaptor = engineStatsAdaptor;
		
		setup(specification);
	}
	
	private void setup(CoordinatorSpecification specification) throws CMMConfigException {
		// First retrieve the Coordinator Policy and setup all control parameters
		AgentPolicy controlPolicy = specification.getControlPolicy();
		
		OntModel controlModel = coordAgent.getConfigurationLoader().load(controlPolicy.getFileNameOrURI());
		controlParameters = new ControlParameters(controlModel);
		
		// Configure CONSERT Engine
		configureCONSERTEngine();
		
		// Build the Command Rule Index
		commandRuleIndex = new CommandRuleIndex(this, controlModel);
		
		// Setup the Command Rule Execution Service.
		commandExecutionService = Executors.newSingleThreadScheduledExecutor();
	}
	
	
	private void configureCONSERTEngine() {
		// STEP 1a: configure CONSERT Engine defaults
		// set default ContextAssertion update configuration
		boolean updateEnabledDefault = controlParameters.defaultUpdateEnabled();
		engineCommandAdaptor.setAssertionInsertActiveByDefault(updateEnabledDefault);
		engineCommandAdaptor.setAssertionInferenceActiveByDefault(updateEnabledDefault);
		
		// set default RUN_WINDOW parameter
		long defaultRunWindow = controlParameters.defaultRunWindow();
		engineCommandAdaptor.setDefaultQueryRunWindow(defaultRunWindow);
		engineCommandAdaptor.setDefaultInferenceRunWindow(defaultRunWindow);
		
		// set default integrity/uniqueness constraint resolution services
		engineCommandAdaptor.setDefaultIntegrityConstraintResolution(controlParameters.defaultIntegrityResolutionStrategy());
		engineCommandAdaptor.setDefaultUniquenessConstraintResolution(controlParameters.defaultUniquenessResolutionStrategy());
		
		// STEP 1b: configure CONSERT Engine specifics
		Map<Resource, Boolean> specificUpdateEnabled = controlParameters.specificUpdateEnabled();
		for (Resource assertionRes : specificUpdateEnabled.keySet()) {
			engineCommandAdaptor.setAssertionActive(assertionRes, specificUpdateEnabled.get(assertionRes));
		}
		
		Map<Resource, String> specificIntegrityConstraintResolution = controlParameters.specificIntegrityConstraintResolutionStrategy();
		for (Resource assertionRes : specificIntegrityConstraintResolution.keySet()) {
			engineCommandAdaptor.setSpecificIntegrityConstraintResolution(assertionRes, specificIntegrityConstraintResolution.get(assertionRes));
		}
		
		Map<Resource, String> specificUniquenessConstraintResolution = controlParameters.specificUniquenessConstraintResolutionStrategy();
		for (Resource assertionRes : specificIntegrityConstraintResolution.keySet()) {
			engineCommandAdaptor.setSpecificUniquenessConstraintResolution(assertionRes, specificUniquenessConstraintResolution.get(assertionRes));
		}
		
		Map<Resource, String> specificValueConstraintResolution = controlParameters.specificValueConstraintResolutionStrategy();
		for (Resource assertionRes : specificValueConstraintResolution.keySet()) {
			engineCommandAdaptor.setSpecificValueConstraintResolution(assertionRes, specificValueConstraintResolution.get(assertionRes));
		}
    }
	
	private void scheduleCommandRuleExecution() {
		// This service will run once every 1/2 of RUN_WINDOW period.
		// However, for the rules that reference any context model assertions, they will only be triggered if 
		// there was an update to those assertions since the last execution.
		long runWindow = controlParameters.defaultRunWindow();
		long commandExecPeriod = runWindow / 2;
		
		commandExecutionService.scheduleAtFixedRate(new CommandRuleExecTask(this), 0, commandExecPeriod, TimeUnit.SECONDS);
	}
	
	// COMMAND EXECUTION
	////////////////////////////////////////////////////////////////////////////////////
	private Map<Resource, Boolean> assertionChangedTracker = new ConcurrentHashMap<Resource, Boolean>(); 
	private ScheduledExecutorService commandExecutionService;
	
	void notifyAssertionUpdated(Resource assertionResource) {
		assertionChangedTracker.put(assertionResource, true);
	}
	
	void resetAssertionChangeTracker() {
		for (OntProperty commandRuleProp : commandRuleIndex.getCommandRuleProperties()) {
			for (CommandRule commandRule : commandRuleIndex.getCommandRules(commandRuleProp)) {
				for (Resource assertionRes : commandRule.getReferencedAssertions()) {
					assertionChangedTracker.put(assertionRes, false);
				}
			}
		}
	}
	
	boolean assertionChanged(Resource resource) {
		return assertionChangedTracker.get(resource) == null ? false : assertionChangedTracker.get(resource);
	}
	
	void setCommandRuleServiceActive(boolean active) {
		if (!active && commandExecutionService != null) {
			commandExecutionService.shutdown();
		}
		else if (active) {
			if (commandExecutionService == null) {
				commandExecutionService = Executors.newSingleThreadScheduledExecutor();
			}
			
			scheduleCommandRuleExecution();
		}
	}
	
	private static class CommandRuleExecTask implements Runnable {
		private CommandManager manager;
		private List<CommandResult> commandResultList;
		
		CommandRuleExecTask(CommandManager manager) {
			this.manager = manager;
			commandResultList = new LinkedList<CommandResult>();
		}
		
		@Override
        public void run() {
	        System.out.println("RUNNING CONTROL MANAGEMENT");
			
			// STEP 1: take snapshot of ContextStore and Statistics
			Dataset contextStore = manager.getEngineCommandAdaptor().getRuntimeContextStore();
			Model statsModel = buildStatsModel();
		
			//System.out.println("[STATS MODEL] =========================== ");
			//statsModel.write(System.out, "TTL");
			
			contextStore.begin(ReadWrite.READ);
			try {
				// Create a unified model from contextStore and statsModel
				Dataset dataset = DatasetFactory.make(contextStore, statsModel);
				Model queryModel = getUnionModel(dataset);
				
				ARQFactory.set(new CommandARQFactory(dataset));
				
				// STEP 2: get all commandRules in order and execute them
				for (OntProperty commandRuleProp : manager.commandRuleIndex.getCommandRuleProperties()) {
					for (CommandRule commandRule : manager.commandRuleIndex.getCommandRules(commandRuleProp)) {
						boolean execute = true;
						
						// Check if the commandRule references any ContextAssertions. If true,
						// only execute the rule if there was at least an update to a referenced assertion
						/*
						Set<Resource> referencedAssertions = commandRule.getReferencedAssertions(); 
						if (!referencedAssertions.isEmpty()) {
							boolean updated = false;
							for (Resource assertionRes : referencedAssertions) {
								if (manager.assertionChanged(assertionRes)) {
									updated = true;
									break;
								}
							}
							
							execute = updated;
							System.out.println("[INFO " + getClass().getSimpleName() + "] "
									+ "WE HAVE A COMMAND THAT REFERENCES A CONTEXT-ASSERTION which has update status: " + updated);
						}
						*/
						
						// Insert the results for this command in the global list, overwriting all the
						// results with which it conflicts; this is essentially the ordered preference 
						// mechanism that we employ.
						List<CommandResult> results = commandRule.execute(queryModel);
						if (results != null) {
							for (CommandResult cmdResult : results) {
								boolean overwritten = false;
								
								for (int i = 0; i < commandResultList.size(); i++) {
									if (cmdResult.conflictsResult(commandResultList.get(i))) {
										commandResultList.set(i, cmdResult);
										overwritten = true;
									}
								}
								
								if (!overwritten) {
									commandResultList.add(cmdResult);
								}
							}
						}
					}
				}
				
				System.out.println("[COMMAND MANAGER] ===============================");
				System.out.println("Collected command results");
				System.out.println(commandResultList);
				System.out.println("=======================================");
				
				// STEP 3: handle command results
				for (CommandResult cmdResult : commandResultList) {
					cmdResult.apply(manager);
				}
				
				// STEP 4: clear command results, reset assertion change tracker and null-out the working models
				// for garbage collection
				commandResultList.clear();
				manager.resetAssertionChangeTracker();
				
				queryModel = null;
				statsModel = null;
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			finally {
				contextStore.end();
			}
		}

		private Model buildStatsModel() {
	        EngineInferenceStats inferenceStats = manager.getEngineStatsAdaptor().getInferenceStatistics();
	        EngineQueryStats queryStats = manager.getEngineStatsAdaptor().getQueryStatistics();
	        
	        Model statsModel = ModelFactory.createDefaultModel();
	        Map<Resource, Resource> assertionStatsMap = new HashMap<Resource, Resource>();
	        
	        // ======== STEP 1) Fill model with enabled/disabled status for the sensed and derived ContextAssertions
	        List<Resource> enabledAssertions = manager.getEngineStatsAdaptor().getEnabledAssertions();
	        for (Resource assertionRes : enabledAssertions) {
	        	Resource statsResource = assertionStatsMap.get(assertionRes);
	        	if (statsResource == null) {
					statsResource = createAssertionStatsResource(assertionRes, statsModel);
					assertionStatsMap.put(assertionRes, statsResource);
				}
	        	
	        	statsResource.addLiteral(CoordConf.isEnabledAssertion, true);
	        }
	        
	        // ======== STEP 2) Fill model with query statistics
			// nrQueries
	        Map<Resource, Integer> nrQueryMap = queryStats.nrQueries();
			for (Resource assertionRes : nrQueryMap.keySet()) {
				Resource statsResource = assertionStatsMap.get(assertionRes);
				if (statsResource == null) {
					statsResource = createAssertionStatsResource(assertionRes, statsModel);
					assertionStatsMap.put(assertionRes, statsResource);
				}
				
				int nrQueries = nrQueryMap.get(assertionRes) == null ? 0 : nrQueryMap.get(assertionRes).intValue();
				statsResource.addLiteral(CoordConf.nrQueries, nrQueries);
			}
			
			// nrSubscriptions
			Map<Resource, Integer> nrSubscriptionsMap = queryStats.nrSubscriptions();
			for (Resource assertionRes : nrSubscriptionsMap.keySet()) {
				Resource statsResource = assertionStatsMap.get(assertionRes);
				if (statsResource == null) {
					statsResource = createAssertionStatsResource(assertionRes, statsModel);
					assertionStatsMap.put(assertionRes, statsResource);
				}
				
				int nrSubs = nrSubscriptionsMap.get(assertionRes) == null ? 0 : nrSubscriptionsMap.get(assertionRes).intValue();
				statsResource.addLiteral(CoordConf.nrSubscriptions, nrSubs);
			}
			
			// nr. successful queries
			Map<Resource, Integer> nrSuccessfulQueryMap = queryStats.nrSuccessfulQueries();
			for (Resource assertionRes : nrSuccessfulQueryMap.keySet()) {
				Resource statsResource = assertionStatsMap.get(assertionRes);
				if (statsResource == null) {
					statsResource = createAssertionStatsResource(assertionRes, statsModel);
					assertionStatsMap.put(assertionRes, statsResource);
				}
				
				int nrQueries = nrSubscriptionsMap.get(assertionRes) == null ? 0 : nrSuccessfulQueryMap.get(assertionRes).intValue();
				statsResource.addLiteral(CoordConf.nrSuccessfulQueries, nrQueries);
			}
			
			// time since last query
			Map<Resource, Long> timeSincelastQueryMap = queryStats.timeSinceLastQuery();
			for (Resource assertionRes : timeSincelastQueryMap.keySet()) {
				Resource statsResource = assertionStatsMap.get(assertionRes);
				if (statsResource == null) {
					statsResource = createAssertionStatsResource(assertionRes, statsModel);
					assertionStatsMap.put(assertionRes, statsResource);
				}
				
				long timeElapsed = timeSincelastQueryMap.get(assertionRes) == null ? 0 : timeSincelastQueryMap.get(assertionRes).longValue();
				long timeElapsedSeconds = timeElapsed / 1000;
				statsResource.addLiteral(CoordConf.timeSinceLastQuery, timeElapsedSeconds);
			}
			
			// ======== STEP 3) Fill model with query statistics
			// nrInferences
			Map<ContextDerivationRule, Integer> nrDerivationsMap = inferenceStats.nrDerivations();
			for (ContextDerivationRule derivationRule : nrDerivationsMap.keySet()) {
				Resource derivedRes = derivationRule.getDerivedAssertion().getOntologyResource();
				
				Resource infResource = assertionStatsMap.get(derivedRes);
				if (infResource == null) {
					infResource = createAssertionStatsResource(derivedRes, statsModel);
					assertionStatsMap.put(derivedRes, infResource);
					
					int ct = nrDerivationsMap.get(derivationRule) == null ? 0 : nrDerivationsMap.get(derivationRule).intValue();
					infResource.addLiteral(CoordConf.nrDerivations, ct);
				}
				
				int ct = nrDerivationsMap.get(derivationRule) == null ? 0 : nrDerivationsMap.get(derivationRule).intValue();
				Statement nrDerivationsStmt = infResource.getProperty(CoordConf.nrDerivations);
				if (nrDerivationsStmt == null) {
					infResource.addLiteral(CoordConf.nrDerivations, ct);
				}
				else {
					ct += nrDerivationsStmt.getInt();
					nrDerivationsStmt.changeLiteralObject(ct);
				}
			}
			
			
			// nr successful Inferences
			Map<ContextDerivationRule, Integer> nrSuccessDerivationsMap = inferenceStats.nrSuccessfulDerivations();
			for (ContextDerivationRule derivationRule : nrSuccessDerivationsMap.keySet()) {
				Resource derivedRes = derivationRule.getDerivedAssertion().getOntologyResource();
				
				Resource infResource = assertionStatsMap.get(derivedRes);
				if (infResource == null) {
					infResource = createAssertionStatsResource(derivedRes, statsModel);
					assertionStatsMap.put(derivedRes, infResource);
					
					int ct = nrSuccessDerivationsMap.get(derivationRule) == null ? 0 : nrSuccessDerivationsMap.get(derivationRule).intValue();
					infResource.addLiteral(CoordConf.nrSuccessfulDerivations, ct);
				}
				
				int ct = nrSuccessDerivationsMap.get(derivationRule) == null ? 0 : nrSuccessDerivationsMap.get(derivationRule).intValue();
				Statement nrDerivationsStmt = infResource.getProperty(CoordConf.nrSuccessfulDerivations);
				if (nrDerivationsStmt == null) {
					infResource.addLiteral(CoordConf.nrSuccessfulDerivations, ct);
				}
				else {
					ct += nrDerivationsStmt.getInt();
					nrDerivationsStmt.changeLiteralObject(ct);
				}
			}
			
			// set last derived resource
			if (inferenceStats.lastDerivation() != null) {
				Resource lastDerivationRes = statsModel.createResource(CoordConf.LastDerivation);
				lastDerivationRes.addProperty(CoordConf.forContextAssertion, 
						inferenceStats.lastDerivation().getDerivedAssertion().getOntologyResource());
			}
			
			return statsModel;
        }
		
		private Resource createAssertionStatsResource(Resource assertionResource, Model statsModel) {
			Resource statsResource = statsModel.createResource(CoordConf.AssertionSpecificStatistic);
			statsResource.addProperty(CoordConf.forContextAssertion, assertionResource);
			
			ContextAssertionType assertionAcquisitionType = manager.getEngineCommandAdaptor().getAssertionType(assertionResource); 
			statsResource.addProperty(CoordConf.hasAcquisitionType, ResourceFactory.createResource(assertionAcquisitionType.getTypeURI()));
			
			boolean derived = assertionAcquisitionType == ContextAssertionType.Derived; 
			statsResource.addLiteral(CoordConf.isDerivedAssertion, derived);
			
			return statsResource;
		}
		
		private static Model getUnionModel(Dataset dataset) {
			MultiUnion union = new MultiUnion();
			
			// add all the named graphs
			Iterator<String> namedModels = dataset.listNames();
			for (; namedModels.hasNext();) {
				union.addGraph(dataset.getNamedModel(namedModels.next()).getGraph());
			}
			
			// add the default graph
			union.addGraph(dataset.getDefaultModel().getGraph());
			
			return ModelFactory.createModelForGraph(union);
		}
	}
}
