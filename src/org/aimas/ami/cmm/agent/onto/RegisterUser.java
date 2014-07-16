package org.aimas.ami.cmm.agent.onto;



/**
* Protege name: RegisterUser
* @author OntologyBeanGenerator v4.1
* @version 2014/07/14, 15:42:55
*/
public interface RegisterUser extends jade.content.AgentAction {

   /**
   * The CtxUser agent that needs to be registered.
   * Protege name: user
   */
   public void setUser(jade.core.AID value);
   public jade.core.AID getUser();

}
