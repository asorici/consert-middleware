package org.aimas.ami.cmm.agent.onto;



/**
* Protege name: QueryBaseItem
* @author OntologyBeanGenerator v4.1
* @version 2014/12/16, 20:08:31
*/
public interface QueryBaseItem extends jade.content.Concept {

   /**
   * Indicates the address of a CtxQueryHandler agent that can be used to respond to the query 
   * that needs to be forwarded.
   * Protege name: queryHandler
   */
   public void setQueryHandler(jade.core.AID elem);
   public jade.core.AID getQueryHandler();

   /**
   * The domainValue URI which acts as the computed upper bound for the forwarded query.
   * Protege name: queryUpperBound
   */
   public void setQueryUpperBound(String value);
   public String getQueryUpperBound();
   
   /**
    * The domainValue URI which acts as the computed lower bound for the forwarded query.
    * Protege name: queryLowerBound
    */
    public void setQueryLowerBound(String value);
    public String getQueryLowerBound();
}
