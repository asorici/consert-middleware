package org.aimas.ami.cmm.agent.onto;


import jade.util.leap.*;

/**
* Contains the number of query handlers to which a given query/subscription has to be forwarded.
For each queryHandler, the adapted form of the query is indicated (e.g. for an upper limit broadcast query, the upper limit is always adapted during each hop).
* Protege name: QueryBase
* @author OntologyBeanGenerator v4.1
* @version 2014/12/16, 20:08:31
*/
public interface QueryBase extends jade.content.Predicate {

   /**
   * Protege name: baseItems
   */
   public void addBaseItem(QueryBaseItem elem);
   public boolean removeBaseItem(QueryBaseItem elem);
   public void clearAllBaseItems();
   public Iterator getAllBaseItems();
   public List getBaseItems();
   public void setBaseItems(List l);

}
