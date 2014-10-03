package org.aimas.ami.cmm.agent.onto;



/**
* Protege name: QueryResult
* @author OntologyBeanGenerator v4.1
* @version 2014/09/17, 23:27:36
*/
public interface UserQueryResult extends jade.content.Predicate {

   /**
   * Protege name: isAsk
   */
   public void setIsAsk(boolean value);
   public boolean getIsAsk();

   /**
   * Protege name: errorMessage
   */
   public void setErrorMessage(String value);
   public String getErrorMessage();

   /**
   * Protege name: queryResultSet
   */
   public void setQueryResultSet(String value);
   public String getQueryResultSet();

   /**
   * Protege name: askResult
   */
   public void setAskResult(boolean value);
   public boolean getAskResult();

}
