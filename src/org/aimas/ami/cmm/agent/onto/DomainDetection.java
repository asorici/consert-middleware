package org.aimas.ami.cmm.agent.onto;



/**
* Message sent by an OrgMgr to a local (on the same machine) CtxSensor or CtxUser agent when the mobile machine they are running on enters a new ContextDomain.
* Protege name: DomainDetection
* @author OntologyBeanGenerator v4.1
* @version 2014/07/14, 15:42:54
*/
public interface DomainDetection extends jade.content.Concept {

   /**
   * The detected ContextDomain.
   * Protege name: domain
   */
   public void setDomain(ContextDomain value);
   public ContextDomain getDomain();

}
