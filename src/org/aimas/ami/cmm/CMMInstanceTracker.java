package org.aimas.ami.cmm;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.aimas.ami.cmm.CMMInstanceTracker.CMMInstanceBundleWrapper;
import org.aimas.ami.contextrep.resources.CMMConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.util.tracker.BundleTracker;

public class CMMInstanceTracker extends BundleTracker<CMMInstanceBundleWrapper> {
	public static final String DEFAULT_INSTANCE_BUNDLE_NAME = "cmm-instance-default";
	//public static final String JADE_BUNDLE_SYMBOLIC_NAME = "jade.jadeOsgi";
	//public static final String AGENT_BUNDLE_SYMBOLIC_NAME = "consert-middleware.agent-bundle";
	
	
	private CMMInstanceBundleWrapper defaultInstanceBundle;
	private Map<ContextDomainInfoWrapper, CMMInstanceBundleWrapper> cmmInstanceMap;
	
	/**
	 * Constructs a tracker for CONSERT CMM-Instance bundles. A corresponding wrapper is returned
	 * indicating the ContextDimension and ContextDomain values for which the CMM Instance is considered,
	 * as well as the bundle location and deployment state. 
	 * @param context The bundle context to be used by this tracker.
	 */
	public CMMInstanceTracker(BundleContext context) {
	    super(context, Bundle.INSTALLED | Bundle.ACTIVE, null);
	    
	    cmmInstanceMap = new HashMap<ContextDomainInfoWrapper, CMMInstanceBundleWrapper>();
    }
	
	/**
	 * Retrieve the default CMM Instance bundle that must exist in every deployment. If it exists, the bundle will already be
	 * installed and active, prior to this call, so this method will return it. Otherwise it will return null.  
	 * @return The default CMM Instance bundle or null if no such bundle exists. 
	 */
	public Bundle getDefaultInstanceBundle() {
		if (defaultInstanceBundle != null) {
			return defaultInstanceBundle.getCmmInstanceBundle();
		}
		
		return null;
	}
	
	public Bundle getCMMInstanceBundle(String applicationId, String contextDimensionURI, String contextDomainValueURI) {
		ContextDomainInfoWrapper domainWrapper = new ContextDomainInfoWrapper(contextDimensionURI, contextDomainValueURI, applicationId);
		
		// First try the default provisioning group bundle
		if (defaultInstanceBundle.matchesContextDomain(domainWrapper)) {
			return defaultInstanceBundle.getCmmInstanceBundle();
		}
		
		// Then look for specifics if the default doesn't match
		CMMInstanceBundleWrapper bundleWrapper = cmmInstanceMap.get(domainWrapper);
		return bundleWrapper != null ? bundleWrapper.getCmmInstanceBundle() : null;
	}
	
	@Override
	public CMMInstanceBundleWrapper addingBundle(Bundle bundle, BundleEvent event) {
		// Determine if this is a valid CONSERT CMM-Instance bundle and check to see if it is the defaultCMMInstance bundle
		Dictionary<String, String> headers = bundle.getHeaders();
		
		String applicationId = headers.get(CMMConstants.CONSERT_APPLICATION_ID_HEADER);
		String contextDimensionURI = headers.get(CMMConstants.CONSERT_CONTEXT_DIMENSION_HEADER);
		String contextDomainValueURI = headers.get(CMMConstants.CONSERT_CONTEXT_DOMAIN_HEADER);
		
		String bundleName = headers.get(Constants.BUNDLE_NAME);
		
		// First check for the existence of the default instance bundle
		if (bundleName.equals(DEFAULT_INSTANCE_BUNDLE_NAME)) {
			if (defaultInstanceBundle == null) {
				defaultInstanceBundle = new CMMInstanceBundleWrapper(new ContextDomainInfoWrapper(contextDimensionURI, contextDomainValueURI, applicationId), bundle);
			}
			else {
				defaultInstanceBundle.updateCmmInstanceBundle(bundle);
			}
			
			return defaultInstanceBundle;
		}
		// Otherwise there must be an applicationId and (optionally) a contextDimension and a contextDomainValueURI value
		else if (applicationId != null) {
			ContextDomainInfoWrapper contextDomainWrapper = new ContextDomainInfoWrapper(contextDimensionURI, contextDomainValueURI, applicationId);
			CMMInstanceBundleWrapper instanceBundleWrapper = cmmInstanceMap.get(contextDomainWrapper);
			
			if (instanceBundleWrapper == null) {
				instanceBundleWrapper = new CMMInstanceBundleWrapper(contextDomainWrapper, bundle);
				cmmInstanceMap.put(contextDomainWrapper, instanceBundleWrapper);
			}
			else {
				instanceBundleWrapper.updateCmmInstanceBundle(bundle);
			}
			
			return instanceBundleWrapper;
		}
		
		// otherwise it is not a valid CONSERT CMM Instance bundle, so return null
		return null;
	}
	
	
	@Override
	public void modifiedBundle(Bundle bundle, BundleEvent event, final CMMInstanceBundleWrapper cmmInstanceBundleWrapper) {
		Dictionary<String, String> headers = bundle.getHeaders();
		String applicationId = headers.get(CMMConstants.CONSERT_APPLICATION_ID_HEADER);
		String contextDimensionURI = headers.get(CMMConstants.CONSERT_CONTEXT_DIMENSION_HEADER);
		String contextDomainValueURI = headers.get(CMMConstants.CONSERT_CONTEXT_DOMAIN_HEADER);
		String bundleName = headers.get(Constants.BUNDLE_NAME);
		
		if (bundleName.equals(DEFAULT_INSTANCE_BUNDLE_NAME)) {
			defaultInstanceBundle.updateCmmInstanceBundle(bundle);
		}
		else if (applicationId != null) {
			ContextDomainInfoWrapper contextDomainWrapper = new ContextDomainInfoWrapper(contextDimensionURI, contextDomainValueURI, applicationId);
			CMMInstanceBundleWrapper instanceBundleWrapper = cmmInstanceMap.get(contextDomainWrapper);
			
			if (instanceBundleWrapper != null) {
				instanceBundleWrapper.updateCmmInstanceBundle(bundle);
			}
		}
	}
	
	
	@Override
	public void removedBundle(Bundle bundle, BundleEvent event, CMMInstanceBundleWrapper deployConfigWrapper) {
		Dictionary<String, String> headers = bundle.getHeaders();
		String applicationId = headers.get(CMMConstants.CONSERT_APPLICATION_ID_HEADER);
		String contextDimensionURI = headers.get(CMMConstants.CONSERT_CONTEXT_DIMENSION_HEADER);
		String contextDomainValueURI = headers.get(CMMConstants.CONSERT_CONTEXT_DOMAIN_HEADER);
		
		if (bundle.getSymbolicName().equals(DEFAULT_INSTANCE_BUNDLE_NAME)) {
			//if (event != null && event.getType() == BundleEvent.UNINSTALLED) {
				defaultInstanceBundle = null;
			//}
		}
		else if (applicationId != null) {
			ContextDomainInfoWrapper contextDomainWrapper = new ContextDomainInfoWrapper(contextDimensionURI, contextDomainValueURI, applicationId);
			
			//if (event != null && event.getType() == BundleEvent.UNINSTALLED) {
				cmmInstanceMap.remove(contextDomainWrapper);
			//}
		}
	}
	
	
	public static class CMMInstanceBundleWrapper {
		private ContextDomainInfoWrapper contextDomainInfo;
		private Bundle cmmInstanceBundle;
		
		public CMMInstanceBundleWrapper(ContextDomainInfoWrapper contextDomainInfo, Bundle cmmInstanceBundle) {
	        
			this.contextDomainInfo = contextDomainInfo;
	        this.cmmInstanceBundle = cmmInstanceBundle;
        }
		
		public String getContextDimensionURI() {
			return contextDomainInfo.getContextDimensionURI();
		}

		public String getContextDomainValueURI() {
			return contextDomainInfo.getContextDomainValueURI();
		}
		
		public boolean matchesContextDomain(ContextDomainInfoWrapper otherDomainInfo) {
			return contextDomainInfo.equals(otherDomainInfo);
		}
		
		public Bundle getCmmInstanceBundle() {
			return cmmInstanceBundle;
		}

		public int getCmmInstanceBundleState() {
			return cmmInstanceBundle.getState();
		}

		public void updateCmmInstanceBundle(Bundle bundle) {
			this.cmmInstanceBundle = bundle;
		}
	}
	
}
