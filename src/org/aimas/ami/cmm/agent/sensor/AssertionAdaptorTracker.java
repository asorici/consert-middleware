package org.aimas.ami.cmm.agent.sensor;

import org.aimas.ami.cmm.sensing.ContextAssertionAdaptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class AssertionAdaptorTracker extends ServiceTracker<ContextAssertionAdaptor, ContextAssertionAdaptor> {
	private AssertionManager assertionManager;
	
	private static Filter getAdaptorFilter(BundleContext context, String adaptorClassName, 
			String assertionResourceURI, String cmmAgentName ) {
		
		String serviceFilter 	= 	"(" + Constants.OBJECTCLASS + "=" + ContextAssertionAdaptor.class.getName() + ")";
		String implFilter 		= 	"(" + ContextAssertionAdaptor.ADAPTOR_IMPL_CLASS + "=" + adaptorClassName + ")";
		String assertionFilter 	= 	"(" + ContextAssertionAdaptor.ADAPTOR_ASSERTION + "=" + assertionResourceURI + ")";
		String agentFilter		= 	"(" + ContextAssertionAdaptor.ADAPTOR_CMM_AGENT + "=" + cmmAgentName + ")";
		
		String filter = "(&" + serviceFilter + implFilter + assertionFilter + agentFilter + ")";
		
		try {
	        return context.createFilter(filter);
        }
        catch (InvalidSyntaxException e) {
	        e.printStackTrace();
	        return null;
        }
	}
	
	public AssertionAdaptorTracker(BundleContext context, String adaptorClassName, 
			String assertionResourceURI, String cmmAgentName) {
	    super(context, getAdaptorFilter(context, adaptorClassName, assertionResourceURI, cmmAgentName), null);
	}
	
	@Override
	public ContextAssertionAdaptor addingService(ServiceReference<ContextAssertionAdaptor> reference) {
		assertionManager.setActive(true);
		
		//System.out.println("["+getClass().getSimpleName()+"] INFO: SETTING ASSERTION ADAPTOR SERVICE FOR: " + reference.getPropertyKeys());
		
		return super.addingService(reference);
	}
	
	@Override
	public void removedService(ServiceReference<ContextAssertionAdaptor> reference, ContextAssertionAdaptor adaptor) {
		assertionManager.setActive(false);
		super.removedService(reference, adaptor);
	}
	
	public void setAssertionManager(AssertionManager assertionManager) {
	    this.assertionManager = assertionManager;
    }
}