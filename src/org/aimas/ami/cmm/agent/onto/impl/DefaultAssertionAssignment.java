package org.aimas.ami.cmm.agent.onto.impl;


import jade.util.leap.*;
import org.aimas.ami.cmm.agent.onto.*;

/**
* Protege name: AssertionAssignment
* @author OntologyBeanGenerator v4.1
* @version 2014/07/25, 19:43:09
*/
public class DefaultAssertionAssignment implements AssertionAssignment {

  private static final long serialVersionUID = -8749049500310255927L;

  private String _internalInstanceName = null;

  public DefaultAssertionAssignment() {
    this._internalInstanceName = "";
  }

  public DefaultAssertionAssignment(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * The CtxCoord agent to which the ContextAssertions of this assignment can be sent.
   * Protege name: coordinator
   */
   private jade.core.AID coordinator;
   public void setCoordinator(jade.core.AID value) { 
    this.coordinator=value;
   }
   public jade.core.AID getCoordinator() {
     return this.coordinator;
   }

   /**
   * The type of ContextAssertion that a CtxSensor or CtxUser can provide.
   * Protege name: capability
   */
   private List capability = new ArrayList();
   public void addCapability(AssertionDescription elem) { 
     capability.add(elem);
   }
   public boolean removeCapability(AssertionDescription elem) {
     boolean result = capability.remove(elem);
     return result;
   }
   public void clearAllCapability() {
     capability.clear();
   }
   public Iterator getAllCapability() {return capability.iterator(); }
   public List getCapability() {return capability; }
   public void setCapability(List l) {capability = l; }

}
