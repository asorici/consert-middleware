package org.aimas.ami.cmm.agent.onto;



/**
* Protege name: ContextDomain
* @author OntologyBeanGenerator v4.1
* @version 2014/07/14, 15:42:54
*/
public interface ContextDomain extends jade.content.Concept {

   /**
   * The URI of the ContextEntity instance that represents the domain value of the newly detected ContextDomain into which the mobile CtxUser or CtxSensor agent that receives it has moved.
   * Protege name: domainValue
   */
   public void setDomainValue(String value);
   public String getDomainValue();

   /**
   * The URI of the ContextEntity that gives the domain values along the ContextDimension.
   * Protege name: domainEntity
   */
   public void setDomainEntity(String value);
   public String getDomainEntity();

   /**
   * The URI of the ContextAssertion which constitutes the ContextDimension that the OrgMgr agents are managing.
   * Protege name: domainDimension
   */
   public void setDomainDimension(String value);
   public String getDomainDimension();

}
