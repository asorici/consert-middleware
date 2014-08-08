package org.aimas.ami.cmm.agent.sensor;

import org.aimas.ami.cmm.api.ContextAssertionAdaptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class AssertionAdaptorTracker extends ServiceTracker<ContextAssertionAdaptor, ContextAssertionAdaptor> {
	private AssertionManager assertionManager;

	public AssertionAdaptorTracker(BundleContext context, String adaptorClassName) {
	    super(context, adaptorClassName, null);
	}
	
	@Override
	public ContextAssertionAdaptor addingService(ServiceReference<ContextAssertionAdaptor> reference) {
		assertionManager.setActive(true);
		return super.addingService(reference);
	}
	
	@Override
	public void removedService(ServiceReference<ContextAssertionAdaptor> reference, ContextAssertionAdaptor adaptor) {
		assertionManager.setActive(true);
		super.removedService(reference, adaptor);
	}
	
	public void setAssertionManager(AssertionManager assertionManager) {
	    this.assertionManager = assertionManager;
    }
}