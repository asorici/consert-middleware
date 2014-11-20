package org.aimas.ami.cmm;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.aimas.ami.cmm.CMMInstanceTracker.CMMInstanceBundleWrapper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;

public class CMMInstanceTracker extends BundleTracker<CMMInstanceBundleWrapper> {
	public static final String CONSERT_APPLICATION_ID_HEADER 		= "Consert-ApplicationId";
	public static final String CONSERT_CONTEXT_DIMENSION_HEADER 	= "Consert-ContextDimension";
	public static final String CONSERT_CONTEXT_DOMAIN_HEADER 		= "Consert-ContextDomain";
	
	public static final String DEFAULT_INSTANCE_BUNDLE_SYMBOLIC_NAME = "consert-engine.cmm-default-instance";
	public static final String JADE_BUNDLE_SYMBOLIC_NAME = "jade.jadeOsgi";
	public static final String AGENT_BUNDLE_SYMBOLIC_NAME = "consert-middleware.agent-bundle";
	
	
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
		return defaultInstanceBundle.getCmmInstanceBundle();
	}
	
	public Bundle getCMMInstanceBundle(String applicationId, String contextDimensionURI, String contextDomainValueURI) {
		ContextDomainInfoWrapper domainWrapper = new ContextDomainInfoWrapper(contextDimensionURI, contextDomainValueURI, applicationId);
		CMMInstanceBundleWrapper bundleWrapper = cmmInstanceMap.get(domainWrapper);
		
		return bundleWrapper != null ? bundleWrapper.getCmmInstanceBundle() : null;
	}
	
	@Override
	public CMMInstanceBundleWrapper addingBundle(Bundle bundle, BundleEvent event) {
		// Determine if this is a valid CONSERT CMM-Instance bundle and check to see if it is the defaultCMMInstance bundle
		String bundleSymbolicName = bundle.getSymbolicName();
		
		Dictionary<String, String> headers = bundle.getHeaders();
		String applicationId = headers.get(CONSERT_APPLICATION_ID_HEADER);
		String contextDimensionURI = headers.get(CONSERT_CONTEXT_DIMENSION_HEADER);
		String contextDomainValueURI = headers.get(CONSERT_CONTEXT_DOMAIN_HEADER);
		
		// First check for the existence of the default instance bundle
		if (bundleSymbolicName.equals(DEFAULT_INSTANCE_BUNDLE_SYMBOLIC_NAME)) {
			if (defaultInstanceBundle == null) {
				defaultInstanceBundle = new CMMInstanceBundleWrapper(new ContextDomainInfoWrapper(contextDimensionURI, contextDomainValueURI, applicationId), bundle);
			}
			else {
				defaultInstanceBundle.updateCmmInstanceBundle(bundle);
			}
			
			return defaultInstanceBundle;
		}
		// Otherwise there must be a contextDimension, a contextDomainValueURI and an applicationId
		else if (contextDimensionURI != null && contextDomainValueURI != null && applicationId != null) {
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
		String applicationId = headers.get(CONSERT_APPLICATION_ID_HEADER);
		String contextDimensionURI = headers.get(CONSERT_CONTEXT_DIMENSION_HEADER);
		String contextDomainValueURI = headers.get(CONSERT_CONTEXT_DOMAIN_HEADER);
		
		if (bundle.getSymbolicName().equals(DEFAULT_INSTANCE_BUNDLE_SYMBOLIC_NAME)) {
			defaultInstanceBundle.updateCmmInstanceBundle(bundle);
		}
		else if (applicationId != null && contextDimensionURI != null && contextDomainValueURI != null) {
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
		String applicationId = headers.get(CONSERT_APPLICATION_ID_HEADER);
		String contextDimensionURI = headers.get(CONSERT_CONTEXT_DIMENSION_HEADER);
		String contextDomainValueURI = headers.get(CONSERT_CONTEXT_DOMAIN_HEADER);
		
		if (bundle.getSymbolicName().equals(DEFAULT_INSTANCE_BUNDLE_SYMBOLIC_NAME)) {
			if (event.getType() == BundleEvent.UNINSTALLED) {
				defaultInstanceBundle = null;
			}
		}
		else if (applicationId != null && contextDimensionURI != null && contextDomainValueURI != null) {
			ContextDomainInfoWrapper contextDomainWrapper = new ContextDomainInfoWrapper(contextDimensionURI, contextDomainValueURI, applicationId);
			
			if (event.getType() == BundleEvent.UNINSTALLED) {
				cmmInstanceMap.remove(contextDomainWrapper);
			}
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
