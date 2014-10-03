package org.aimas.ami.cmm.agent.onto.impl;


import jade.util.leap.*;
import org.aimas.ami.cmm.agent.onto.*;

/**
* Protege name: AssertionDescription
* @author OntologyBeanGenerator v4.1
* @version 2014/07/31, 13:46:14
*/
public class DefaultAssertionDescription implements AssertionDescription {

  private static final long serialVersionUID = 5438106203733924709L;

  private String _internalInstanceName = null;

  public DefaultAssertionDescription() {
    this._internalInstanceName = "";
  }

  public DefaultAssertionDescription(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return assertionType;
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
   
   
   @Override
   public int hashCode() {
       final int prime = 31;
       int result = 1;
       result = prime * result + ((assertionType == null) ? 0 : assertionType.hashCode());
       
       for (int i = 0; i < annotationType.size(); i++) {
           result = prime * result + ((annotationType.get(i) == null) ? 0 : annotationType.get(i).hashCode());
       }
       
       return result;
   }
   
   @Override
   public boolean equals(Object obj) {
       if (this == obj) {
           return true;
       }
       if (obj == null) {
           return false;
       }
       if (!(obj instanceof DefaultAssertionDescription)) {
           return false;
       }
       DefaultAssertionDescription other = (DefaultAssertionDescription) obj;
       if (assertionType == null) {
           if (other.assertionType != null) {
               return false;
           }
       }
       else if (!assertionType.equals(other.assertionType)) {
           return false;
       }
       
       if (annotationType == null) {
           if (other.annotationType != null) {
               return false;
           }
       }
       else { 
           if (annotationType.size() != other.annotationType.size()) {
               return false;
           }
           
           for (int i = 0; i < annotationType.size(); i++) {
               if (!other.annotationType.contains(annotationType.get(i))) {
                   return false;
               }
           }
       }
       
       return true;
   }
}
