package org.aimas.ami.cmm.agent.onto.impl;


import jade.util.leap.*;
import org.aimas.ami.cmm.agent.onto.*;

/**
* Protege name: QueryBase
* @author OntologyBeanGenerator v4.1
* @version 2014/07/29, 17:38:36
*/
public class DefaultQueryBase implements QueryBase {

  private static final long serialVersionUID = 7138998227437283033L;

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
   * Indicates the address of a CtxQueryHandler agent that can be used to respond to the query that needs to be posed.
   * Protege name: queryHandler
   */
   private List queryHandler = new ArrayList();
   public void addQueryHandler(jade.core.AID elem) { 
     queryHandler.add(elem);
   }
   public boolean removeQueryHandler(jade.core.AID elem) {
     boolean result = queryHandler.remove(elem);
     return result;
   }
   public void clearAllQueryHandler() {
     queryHandler.clear();
   }
   public Iterator getAllQueryHandler() {return queryHandler.iterator(); }
   public List getQueryHandler() {return queryHandler; }
   public void setQueryHandler(List l) {queryHandler = l; }

}
