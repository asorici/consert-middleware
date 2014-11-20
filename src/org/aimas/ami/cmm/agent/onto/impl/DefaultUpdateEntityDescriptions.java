package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Protege name: UpdateEntityDescriptions
* @author OntologyBeanGenerator v4.1
* @version 2014/11/18, 17:50:38
*/
public class DefaultUpdateEntityDescriptions implements UpdateEntityDescriptions {

  private static final long serialVersionUID = -6514229755505387621L;

  private String _internalInstanceName = null;

  public DefaultUpdateEntityDescriptions() {
    this._internalInstanceName = "";
  }

  public DefaultUpdateEntityDescriptions(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: entityContents
   */
   private String entityContents;
   public void setEntityContents(String value) { 
    this.entityContents=value;
   }
   public String getEntityContents() {
     return this.entityContents;
   }

}
