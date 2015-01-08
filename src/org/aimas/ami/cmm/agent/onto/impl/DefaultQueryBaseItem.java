package org.aimas.ami.cmm.agent.onto.impl;


import jade.core.AID;

import org.aimas.ami.cmm.agent.onto.QueryBaseItem;

/**
* Protege name: QueryBaseItem
* @author OntologyBeanGenerator v4.1
* @version 2014/12/16, 20:08:31
*/
public class DefaultQueryBaseItem implements QueryBaseItem {
	
	// TODO: I need to change this to make a single QueryHandler
	
	private static final long serialVersionUID = 220153654612988791L;
	
	private String _internalInstanceName = null;
	
	public DefaultQueryBaseItem() {
		this._internalInstanceName = "";
	}
	
	public DefaultQueryBaseItem(String instance_name) {
		this._internalInstanceName = instance_name;
	}
	
	public String toString() {
		return _internalInstanceName;
	}

	/**
	 * Indicates the address of a CtxQueryHandler agent that can be used to respond to the query that needs to be posed.
	 * Protege name: queryHandler
	 */
	private jade.core.AID queryHandler;
	
	private String queryUpperBound;
	private String queryLowerBound;

	@Override
	public void setQueryHandler(AID elem) {
		this.queryHandler = elem;	
	}
	
	@Override
	public AID getQueryHandler() {
		return queryHandler;
	}
	
	@Override
	public void setQueryUpperBound(String value) {
		this.queryUpperBound = value;
	}
	
	@Override
	public String getQueryUpperBound() {
		return queryUpperBound;
	}
	
	@Override
	public void setQueryLowerBound(String value) {
		this.queryLowerBound = value;
	}
	
	@Override
	public String getQueryLowerBound() {
		return queryLowerBound;
	}

}
