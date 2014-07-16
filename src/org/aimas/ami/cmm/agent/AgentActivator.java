package org.aimas.ami.cmm.agent;

import jade.osgi.service.agentFactory.AgentFactoryService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class AgentActivator implements BundleActivator {
	private AgentFactoryService agentFactory;
	
	
	@Override
    public void start(BundleContext context) throws Exception {
		agentFactory = new AgentFactoryService(); 
		agentFactory.init(context.getBundle());
    }

	@Override
    public void stop(BundleContext context) throws Exception {
	    agentFactory.clean();
    }
	
}
