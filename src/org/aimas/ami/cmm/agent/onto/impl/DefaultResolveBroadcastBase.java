package org.aimas.ami.cmm.agent.onto.impl;


import jade.core.AID;

import org.aimas.ami.cmm.agent.onto.*;

/**
* Protege name: ResolveBroadcastBase
* @author OntologyBeanGenerator v4.1
* @version 2015/03/17, 13:57:23
*/
public class DefaultResolveBroadcastBase implements ResolveBroadcastBase {

	private static final long serialVersionUID = 3798988534798726725L;

	private String _internalInstanceName = null;

	public DefaultResolveBroadcastBase() {
		this._internalInstanceName = "";
	}

	public DefaultResolveBroadcastBase(String instance_name) {
		this._internalInstanceName = instance_name;
	}
	
	public String toString() {
		return _internalInstanceName;
	}
	
	private AID receivedFromAgent;
	@Override
	public void setReceivedFromAgent(AID value) {
		receivedFromAgent = value;
	}
	
	@Override
	public AID getReceivedFromAgent() {
		return receivedFromAgent;
	}
	
	private String broadcastUpperBound;
	@Override
	public void setBroadcastUpperBound(String value) {
		broadcastUpperBound = value;
	}
	
	@Override
	public String getBroadcastUpperBound() {
		return broadcastUpperBound;
	}
	
	private String broadcastLowerBound;
	@Override
	public void setBroadcastLowerBound(String value) {
		broadcastLowerBound = value;
	}
	
	@Override
	public String getBroadcastLowerBound() {
		return broadcastLowerBound;
	}

}
