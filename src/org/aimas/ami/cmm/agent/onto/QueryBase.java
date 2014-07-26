package org.aimas.ami.cmm.agent.onto;


import jade.util.leap.*;

/**
* Protege name: QueryBase
* @author OntologyBeanGenerator v4.1
* @version 2014/07/25, 19:43:09
*/
public interface QueryBase extends jade.content.Predicate {

   /**
   * Indicates the address of a CtxQueryHandler agent that can be used to respond to the query that needs to be posed.
   * Protege name: queryHandler
   */
   public void addQueryHandler(jade.core.AID elem);
   public boolean removeQueryHandler(jade.core.AID elem);
   public void clearAllQueryHandler();
   public Iterator getAllQueryHandler();
   public List getQueryHandler();
   public void setQueryHandler(List l);

}
