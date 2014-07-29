package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Protege name: ContextDomain
* @author OntologyBeanGenerator v4.1
* @version 2014/07/29, 17:38:36
*/
public class DefaultContextDomain implements ContextDomain {

  private static final long serialVersionUID = 7138998227437283033L;

  private String _internalInstanceName = null;

  public DefaultContextDomain() {
    this._internalInstanceName = "";
  }

  public DefaultContextDomain(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * The URI of the ContextEntity instance that represents the domain value of the newly detected ContextDomain into which the mobile CtxUser or CtxSensor agent that receives it has moved.
   * Protege name: domainValue
   */
   private String domainValue;
   public void setDomainValue(String value) { 
    this.domainValue=value;
   }
   public String getDomainValue() {
     return this.domainValue;
   }

   /**
   * The URI of the ContextEntity that gives the domain values along the ContextDimension.
   * Protege name: domainEntity
   */
   private String domainEntity;
   public void setDomainEntity(String value) { 
    this.domainEntity=value;
   }
   public String getDomainEntity() {
     return this.domainEntity;
   }

   /**
   * The URI of the ContextAssertion which constitutes the ContextDimension that the OrgMgr agents are managing.
   * Protege name: domainDimension
   */
   private String domainDimension;
   public void setDomainDimension(String value) { 
    this.domainDimension=value;
   }
   public String getDomainDimension() {
     return this.domainDimension;
   }

}
