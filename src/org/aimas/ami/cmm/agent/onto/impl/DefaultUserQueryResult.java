package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Protege name: QueryResult
* @author OntologyBeanGenerator v4.1
* @version 2014/09/17, 23:27:36
*/
public class DefaultUserQueryResult implements UserQueryResult {

  private static final long serialVersionUID = 7890492462394911173L;

  private String _internalInstanceName = null;

  public DefaultUserQueryResult() {
    this._internalInstanceName = "";
  }

  public DefaultUserQueryResult(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: isAsk
   */
   private boolean isAsk;
   public void setIsAsk(boolean value) { 
    this.isAsk=value;
   }
   public boolean getIsAsk() {
     return this.isAsk;
   }

   /**
   * Protege name: errorMessage
   */
   private String errorMessage;
   public void setErrorMessage(String value) { 
    this.errorMessage=value;
   }
   public String getErrorMessage() {
     return this.errorMessage;
   }

   /**
   * Protege name: queryResultSet
   */
   private String queryResultSet;
   public void setQueryResultSet(String value) { 
    this.queryResultSet=value;
   }
   public String getQueryResultSet() {
     return this.queryResultSet;
   }

   /**
   * Protege name: askResult
   */
   private boolean askResult;
   public void setAskResult(boolean value) { 
    this.askResult=value;
   }
   public boolean getAskResult() {
     return this.askResult;
   }

}
