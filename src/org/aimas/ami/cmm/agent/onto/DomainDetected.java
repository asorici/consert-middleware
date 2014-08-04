package org.aimas.ami.cmm.agent.onto;



/**
* Message sent by an OrgMgr to a local (on the same machine) CtxSensor or CtxUser agent when the mobile machine they are running on enters a new ContextDomain.
* Protege name: DomainDetected
* @author OntologyBeanGenerator v4.1
* @version 2014/07/31, 13:46:14
*/
public interface DomainDetected extends jade.content.Predicate {

   /**
   * The detected ContextDomain.
   * Protege name: domain
   */
   public void setDomain(ContextDomain value);
   public ContextDomain getDomain();

}
