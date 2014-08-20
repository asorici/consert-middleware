package org.aimas.ami.cmm.agent.sensor;

import java.util.List;

import org.aimas.ami.cmm.api.ContextAssertionAdaptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class AssertionAdaptorTracker extends ServiceTracker<ContextAssertionAdaptor, ContextAssertionAdaptor> {
	private AssertionManager assertionManager;
	
	private static Filter getAdaptorFilter(BundleContext context, String adaptorClassName, List<String> sensorIdList) {
		String serviceFilter = "(" + Constants.OBJECTCLASS + "=" + ContextAssertionAdaptor.class.getName() + ")";
		String implFilter = "(" + ContextAssertionAdaptor.ADAPTOR_IMPL_CLASS + "=" + adaptorClassName + ")";
		
		String sensorFilter = "";
		for (String sensorIdInfo : sensorIdList) {
			sensorFilter += "(" + ContextAssertionAdaptor.ADAPTOR_HANDLED_SENSORS + "=" + sensorIdInfo + ")";
		}
		
		String filter = "(&" + serviceFilter + implFilter + sensorFilter + ")";
		
		try {
	        return context.createFilter(filter);
        }
        catch (InvalidSyntaxException e) {
	        e.printStackTrace();
	        return null;
        }
	}
	
	public AssertionAdaptorTracker(BundleContext context, String adaptorClassName, List<String> sensorIdList) {
	    super(context, getAdaptorFilter(context, adaptorClassName, sensorIdList), null);
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