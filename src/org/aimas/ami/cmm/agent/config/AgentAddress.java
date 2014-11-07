package org.aimas.ami.cmm.agent.config;

import jade.core.AID;

import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

public class AgentAddress {
	private String agentLocalName;
	private String agentMTPHost;
	private int agentMTPPort;
	
	private CMMAgentContainer agentContainer;
	private AID agentID;
	
	
	public AgentAddress(String agentLocalName, CMMAgentContainer agentContainer) {
	    this.agentLocalName = agentLocalName;
	    this.agentMTPHost = agentContainer.getMTPHost();
	    this.agentMTPPort = agentContainer.getMTPPort();
    
	    this.agentContainer = agentContainer;
	}
	
	public String getLocalName() {
		return agentLocalName;
	}
	
	public String getMTPHost() {
		return agentMTPHost;
	}
	
	public int getMTPPort() {
		return agentMTPPort;
	}
	
	public CMMAgentContainer getAgentContainer() {
	    return agentContainer;
    }
	
	public AID getAID() {
		if (agentID == null) {
			// If we don't have an agent container, we assume the local name suffices 
			// to construct the correct global AID
			if (agentContainer == null) {
				AID aid = new AID(agentLocalName, false);
				aid.addAddresses(getMTPAddress());
				
				agentID = aid;
			}
			
			// Otherwise, use the platform name from the Container to construct the global AID
			String globalAgentName = agentLocalName + "@" + agentContainer.getPlatformName();
			AID aid = new AID(globalAgentName, true);
			aid.addAddresses(getMTPAddress());
			
			agentID = aid;
		}
		
		return agentID;
	}
	
	public String getMTPAddress() {
		return "http://" + agentMTPHost + ":" + agentMTPPort + "/acc";
	}
	
	
	public static AgentAddress fromConfigurationModel(OntModel cmmConfigurationModel, Resource agentAddressResource) {
		if (agentAddressResource == null)
			return null;
		
		String agentLocalName = agentAddressResource.getProperty(OrgConf.agentName).getString();
		CMMAgentContainer agentContainer = CMMAgentContainer.fromConfigurationModel(cmmConfigurationModel, 
				agentAddressResource.getPropertyResourceValue(OrgConf.agentContainer));
		
		return new AgentAddress(agentLocalName, agentContainer);
	}

	@Override
    public int hashCode() {
	    return getAID().hashCode();
    }
	
	
	@Override
    public boolean equals(Object obj) {
	    if (this == obj) {
		    return true;
	    }
	    
	    if (obj == null) {
		    return false;
	    }
	    
	    if (!(obj instanceof AgentAddress)) {
		    return false;
	    }
	    
	    AgentAddress other = (AgentAddress) obj;
	    if (!getAID().equals(other.getAID())) {
		    return false;
	    }
	    
	    return true;
    }
}
