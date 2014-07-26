package org.aimas.ami.cmm.agent.onto;


import jade.util.leap.*;

/**
* Protege name: AssertionDescription
* @author OntologyBeanGenerator v4.1
* @version 2014/07/25, 19:43:09
*/
public interface AssertionDescription extends jade.content.Concept {

   /**
   * The URI of the ContextAssertion for which the TaskingCommand is intended.
   * Protege name: assertionType
   */
   public void setAssertionType(String value);
   public String getAssertionType();

   /**
   * An optional slot that describes the type (given by URI) of the ContextAnnotations that the CtxSensor or CtxUser is able to send for the associated ContextAssertion (given by the assertionType slot)
   * Protege name: annotationType
   */
   public void addAnnotationType(String elem);
   public boolean removeAnnotationType(String elem);
   public void clearAllAnnotationType();
   public Iterator getAllAnnotationType();
   public List getAnnotationType();
   public void setAnnotationType(List l);

}
