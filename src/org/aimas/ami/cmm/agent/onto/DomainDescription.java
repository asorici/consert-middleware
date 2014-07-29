package org.aimas.ami.cmm.agent.onto;



/**
* Predicate used by a OrgMgr to inform data about the current Context Domain as a response to a InformDomain request from a CMM Agent.
* Protege name: DomainDescription
* @author OntologyBeanGenerator v4.1
* @version 2014/07/29, 17:38:36
*/
public interface DomainDescription extends jade.content.Predicate {

   /**
   * The detected ContextDomain.
   * Protege name: domain
   */
   public void setDomain(ContextDomain value);
   public ContextDomain getDomain();

}
