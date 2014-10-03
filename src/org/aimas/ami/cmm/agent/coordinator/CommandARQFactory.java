package org.aimas.ami.cmm.agent.coordinator;

import org.topbraid.spin.arq.ARQFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * A custom ARQ Factory that will always return the dataset associated with 
 * the combination of ContextStore and the usage statistics model of the CONSERT Engine.
 * This custom factory is used while performing Provisioning Control Command checks.
 * @author alex
 *
 */
public class CommandARQFactory extends ARQFactory {
	private Dataset contextDataset;
	
	/**
	 * Instantiate the CommandARQFactory with the combination of the TDB-backed ContextStore plus the 
	 * statistics model added as the default graph. The 
	 * @param dataset
	 */
	public CommandARQFactory(Dataset dataset) {
		contextDataset = dataset;
	}
	
	/**
	 * Specifies the combined ContextStore + Statistics dataset used for query execution.
	 * The default model with which this is called is the union model of ContextStore + Statistics.
	 * @param defaultModel 
	 * @return the Dataset
	 */
	@Override 
	public Dataset getDataset(Model defaultModel) {
		return contextDataset;
	}
}
