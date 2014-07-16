package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Protege name: RegisterUser
* @author OntologyBeanGenerator v4.1
* @version 2014/07/14, 15:42:55
*/
public class DefaultRegisterUser implements RegisterUser {

  private static final long serialVersionUID = 6722885266148375100L;

  private String _internalInstanceName = null;

  public DefaultRegisterUser() {
    this._internalInstanceName = "";
  }

  public DefaultRegisterUser(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * The CtxUser agent that needs to be registered.
   * Protege name: user
   */
   private jade.core.AID user;
   public void setUser(jade.core.AID value) { 
    this.user=value;
   }
   public jade.core.AID getUser() {
     return this.user;
   }

}
