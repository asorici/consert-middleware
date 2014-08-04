package org.aimas.ami.cmm.agent.onto;



/**
* Message sent by the OrgMgr to a CtxSensor shared between multiple applications when one of those applications is installed, becomes active or inactive.
* Protege name: ApplyConfiguration
* @author OntologyBeanGenerator v4.1
* @version 2014/07/31, 13:46:14
*/
public interface ApplyConfiguration extends jade.content.AgentAction {

   /**
   * The RDF content of the agent specific configuration expressed according to the vocabulary of the OrgMgrConf ontology.
   * Protege name: configContent
   */
   public void setConfigContent(String value);
   public String getConfigContent();

}
