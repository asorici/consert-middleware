package org.aimas.ami.cmm.utils;

import jade.core.Agent;

import org.osgi.framework.BundleContext;

public class JadeOSGIBridgeHelperImpl implements JadeOSGIBridgeHelper {
	private BundleContext bundleContext;
	
	public JadeOSGIBridgeHelperImpl(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
	
	@Override
	public void init(Agent a) {
		// Nothing needs to be done here since the bundle context is passed in the constructor.
		// All the CMM agents are loaded from this bundle, so we do not have to inspect other bundles for agent code.
	}
	
	@Override
	public BundleContext getBundleContext() {
		return bundleContext;
	}
}
