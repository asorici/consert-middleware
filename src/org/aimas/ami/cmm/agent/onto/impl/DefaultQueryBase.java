package org.aimas.ami.cmm.agent.onto.impl;


import jade.util.leap.*;
import org.aimas.ami.cmm.agent.onto.*;

/**
* Contains the number of query handlers to which a given query/subscription has to be forwarded.
For each queryHandler, the adapted form of the query is indicated (e.g. for an upper limit broadcast query, the upper limit is always adapted during each hop).
* Protege name: QueryBase
* @author OntologyBeanGenerator v4.1
* @version 2014/12/16, 20:08:31
*/
public class DefaultQueryBase implements QueryBase {

  private static final long serialVersionUID = 220153654612988791L;

  private String _internalInstanceName = null;

  public DefaultQueryBase() {
    this._internalInstanceName = "";
  }

  public DefaultQueryBase(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: baseItems
   */
   private List baseItems = new ArrayList();
   public void addBaseItem(QueryBaseItem elem) { 
     baseItems.add(elem);
   }
   public boolean removeBaseItem(QueryBaseItem elem) {
     boolean result = baseItems.remove(elem);
     return result;
   }
   public void clearAllBaseItems() {
     baseItems.clear();
   }
   public Iterator getAllBaseItems() {return baseItems.iterator(); }
   public List getBaseItems() {return baseItems; }
   public void setBaseItems(List l) {baseItems = l; }

}
