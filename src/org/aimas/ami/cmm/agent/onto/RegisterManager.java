package org.aimas.ami.cmm.agent.onto;




/**
* A message sent by a child OrgMgr to a parent OrgMgr to register as the manager of a Context Domain that is a child of the one managed by the parent OrgMgr,
* Protege name: RegisterChildManager
* @author OntologyBeanGenerator v4.1
* @version 2014/12/15, 20:38:55
*/
public interface RegisterManager extends jade.content.AgentAction {
	public static final String CHILD 	= "child";
	public static final String ROOT 	= "root";
	
   /**
   * The URI of the ContextEntity instance that represents the domain value of the newly detected ContextDomain into which the mobile CtxUser or CtxSensor agent that receives it has moved.
   * Protege name: domainValue
   */
   public void setDomainValue(String value);
   public String getDomainValue();

   /**
   * The type of CMM agent.
   * Protege name: agent
   */
   public void setAgent(jade.core.AID value);
   public jade.core.AID getAgent();

   /**
   * The URI of the ContextEntity that gives the domain values along the ContextDimension.
   * Protege name: domainEntity
   */
   public void setDomainEntity(String value);
   public String getDomainEntity();
   
   public void setRelationType(String value);
   public String getRelationType();
   
   /**
    * The associated queryHandler agent answering queries for this domain.
    * Protege name: queryHandler
    */
    public void setQueryHandler(jade.core.AID value);
    public jade.core.AID getQueryHandler();
	
    /**
     * The associated coordinator agent performing provisioning coordination for this domain.
     * Protege name: coordinator
     */
    public void setCoordinator(jade.core.AID value);
    public jade.core.AID getCoordinator();
}
