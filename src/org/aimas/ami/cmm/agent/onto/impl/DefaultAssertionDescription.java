package org.aimas.ami.cmm.agent.onto.impl;


import jade.util.leap.*;
import org.aimas.ami.cmm.agent.onto.*;

/**
* Protege name: AssertionDescription
* @author OntologyBeanGenerator v4.1
* @version 2014/07/14, 15:42:54
*/
public class DefaultAssertionDescription implements AssertionDescription {

  private static final long serialVersionUID = 6722885266148375100L;

  private String _internalInstanceName = null;

  public DefaultAssertionDescription() {
    this._internalInstanceName = "";
  }

  public DefaultAssertionDescription(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * The URI of the ContextAssertion for which the TaskingCommand is intended.
   * Protege name: assertionType
   */
   private String assertionType;
   public void setAssertionType(String value) { 
    this.assertionType=value;
   }
   public String getAssertionType() {
     return this.assertionType;
   }

   /**
   * An optional slot that describes the type (given by URI) of the ContextAnnotations that the CtxSensor or CtxUser is able to send for the associated ContextAssertion (given by the assertionType slot)
   * Protege name: annotationType
   */
   private List annotationType = new ArrayList();
   public void addAnnotationType(String elem) { 
     annotationType.add(elem);
   }
   public boolean removeAnnotationType(String elem) {
     boolean result = annotationType.remove(elem);
     return result;
   }
   public void clearAllAnnotationType() {
     annotationType.clear();
   }
   public Iterator getAllAnnotationType() {return annotationType.iterator(); }
   public List getAnnotationType() {return annotationType; }
   public void setAnnotationType(List l) {annotationType = l; }

}
