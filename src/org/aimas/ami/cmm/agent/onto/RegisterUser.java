package org.aimas.ami.cmm.agent.onto;



/**
* The message sent by a CtxUser to a CtxQueryHandler to register a new query/subscription client.
* Protege name: RegisterUser
* @author OntologyBeanGenerator v4.1
* @version 2014/07/31, 13:46:14
*/
public interface RegisterUser extends jade.content.AgentAction {

   /**
   * The CtxUser agent that needs to be registered.
   * Protege name: user
   */
   public void setUser(jade.core.AID value);
   public jade.core.AID getUser();

}
