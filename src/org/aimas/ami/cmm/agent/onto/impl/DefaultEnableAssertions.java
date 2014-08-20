package org.aimas.ami.cmm.agent.onto.impl;


import jade.util.leap.*;
import org.aimas.ami.cmm.agent.onto.*;

/**
* Message sent by a CtxQueryHandler to the CtxCoord to which he is associated when a ContextAssertion for which the query handler receives a query does not exist in the ContextStore.
The message also applies as the reply sent by a CtxCoord to a CtxSensor/CtxUser agent in response to a PublishAssertions message.
* Protege name: EnableAssertions
* @author OntologyBeanGenerator v4.1
* @version 2014/07/31, 13:46:14
*/
public class DefaultEnableAssertions implements EnableAssertions {

  private static final long serialVersionUID = 5438106203733924709L;

  private String _internalInstanceName = null;

  public DefaultEnableAssertions() {
    this._internalInstanceName = "";
  }

  public DefaultEnableAssertions(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * The type of ContextAssertion that a CtxSensor or CtxUser can provide.
   * Protege name: enabledCapability
   */
   private List enabledCapability = new ArrayList();
   public void addEnabledCapability(AssertionCapability elem) { 
     enabledCapability.add(elem);
   }
   public boolean removeEnabledCapability(AssertionCapability elem) {
     boolean result = enabledCapability.remove(elem);
     return result;
   }
   public void clearAllEnabledCapability() {
     enabledCapability.clear();
   }
   public Iterator getAllEnabledCapability() {return enabledCapability.iterator(); }
   public List getEnabledCapability() {return enabledCapability; }
   public void setEnabledCapability(List l) {enabledCapability = l; }

}
