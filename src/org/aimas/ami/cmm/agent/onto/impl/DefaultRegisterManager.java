package org.aimas.ami.cmm.agent.onto.impl;


import jade.core.AID;

import org.aimas.ami.cmm.agent.onto.*;

/**
* A message sent by a child OrgMgr to a parent OrgMgr to register as the manager of a Context Domain that is a child of the one managed by the parent OrgMgr,
* Protege name: RegisterChildManager
* @author OntologyBeanGenerator v4.1
* @version 2014/12/15, 20:38:55
*/
public class DefaultRegisterManager implements RegisterManager {

	private static final long serialVersionUID = -6715593102208904040L;
	
	private String _internalInstanceName = null;
	
	public DefaultRegisterManager() {
		this._internalInstanceName = "";
	}
	
	public DefaultRegisterManager(String instance_name) {
		this._internalInstanceName = instance_name;
	}
	
	public String toString() {
		return _internalInstanceName;
	}
	
	/**
	 * The URI of the ContextEntity instance that represents the domain value of
	 * the newly detected ContextDomain into which the mobile CtxUser or
	 * CtxSensor agent that receives it has moved. Protege name: domainValue
	 */
	private String domainValue;
	
	public void setDomainValue(String value) {
		this.domainValue = value;
	}
	
	public String getDomainValue() {
		return this.domainValue;
	}
	
	/**
	 * The type of CMM agent. Protege name: agent
	 */
	private jade.core.AID agent;
	
	public void setAgent(jade.core.AID value) {
		this.agent = value;
	}
	
	public jade.core.AID getAgent() {
		return this.agent;
	}
	
	/**
	 * The URI of the ContextEntity that gives the domain values along the
	 * ContextDimension. Protege name: domainEntity
	 */
	private String domainEntity;
	
	public void setDomainEntity(String value) {
		this.domainEntity = value;
	}
	
	public String getDomainEntity() {
		return this.domainEntity;
	}
	
	/**
	 * Protege name: relationType
	 */
	private String relationType;
	
	@Override
	public void setRelationType(String value) {
		this.relationType = value;
	}
	
	@Override
	public String getRelationType() {
		return relationType;
	}
	
	/**
	 * Protege name: queryHandler
	 */
	private AID queryHandler;
	
	@Override
	public void setQueryHandler(AID value) {
		this.queryHandler = value;
	}
	
	@Override
	public AID getQueryHandler() {
		return queryHandler;
	}
}
