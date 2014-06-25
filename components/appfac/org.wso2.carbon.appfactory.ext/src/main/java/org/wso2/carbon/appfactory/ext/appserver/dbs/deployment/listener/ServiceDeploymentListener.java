package org.wso2.carbon.appfactory.ext.appserver.dbs.deployment.listener;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.engine.AxisObserver;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.application.mgt.stub.ApplicationManagementServiceStub;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;
import org.wso2.carbon.appfactory.eventing.EventBuilderUtil;
import org.wso2.carbon.appfactory.eventing.EventNotifier;
import org.wso2.carbon.appfactory.ext.internal.ServiceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.TenantManager;

import javax.xml.namespace.QName;
import java.util.ArrayList;

/**
 * @scr.component name="org.wso2.carbon.appfactory.appserver.dbs.deployment.listener.ServiceDeploymentListener"
 * immediate="true"
 *
 */

public class ServiceDeploymentListener implements AxisObserver {

    private static final Log log = LogFactory.getLog(ServiceDeploymentListener.class);

//    protected void activate(ComponentContext ctxt) {
//        log.info("************************************* service deployment listener activated");
//        BundleContext bundleCtx = ctxt.getBundleContext();
//
//        // Publish the OSGi service
//        Dictionary props = new Hashtable();
//        props.put(CarbonConstants.AXIS2_CONFIG_SERVICE, AxisObserver.class.getName());
//        bundleCtx.registerService(AxisObserver.class.getName(), this, props);
//
//        PreAxisConfigurationPopulationObserver preAxisConfigObserver =
//                new PreAxisConfigurationPopulationObserver() {
//                    public void createdAxisConfiguration(AxisConfiguration axisConfiguration) {
//                        axisConfiguration.addObservers(new ServiceDeploymentListener());
//                    }
//                };
//        bundleCtx.registerService(PreAxisConfigurationPopulationObserver.class.getName(),
//                preAxisConfigObserver, null);



//    }


    @Override
    public void init(AxisConfiguration axisConfiguration) {

    }

    @Override
    public void serviceUpdate(AxisEvent axisEvent, AxisService axisService) {
        int eventType = axisEvent.getEventType();
        String serviceName = axisService.getName();
	    String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
	    String stage= System.getProperty("stratos.stage");

        if (eventType == AxisEvent.SERVICE_DEPLOY) {
            if (!JavaUtils.isTrue(axisService.getParameterValue(
                    CarbonConstants.HIDDEN_SERVICE_PARAM_NAME))) {
                log.info("Deploying Axis2 service: " + serviceName);
                String infoTitle = "Application " + serviceName + " is successfully deployed.";
                String infoMessage = "Application " + serviceName + " can be accessed via the URL ";
                try {
                    EventNotifier.getInstance().notify(EventBuilderUtil.buildObtainDbsDeploymentStatusEvent(serviceName,
                            infoTitle, infoMessage, Event.Category.INFO));
                } catch (AppFactoryEventException e) {
                    log.error("Failed to notify the Service Deploy deployment success event ", e);
                }
            } else if (log.isDebugEnabled()) {
                log.debug("Deploying hidden Axis2 service : " + serviceName);
            }

	        if (tenantDomain == MultitenantConstants.SUPER_TENANT_DOMAIN_NAME){
		        return;
	        }

	        TenantManager manager = ServiceHolder.getInstance().getRealmService().getTenantManager();
	        try {
		        int tenantId = manager.getTenantId(tenantDomain);
		        String afUrl = AppFactoryUtil.getAppfactoryConfiguration().
				        getFirstProperty("ServerUrls.AppFactory");
		        ApplicationManagementServiceStub stub =
				        new ApplicationManagementServiceStub(afUrl + "ApplicationManagementService");
		        stub._getServiceClient().removeHeaders();
		        stub._getServiceClient()
		            .addStringHeader(new QName("http://mutualssl.carbon.wso2.org", "UserName",
		                                       "tns"), manager.getTenant(tenantId).getAdminName() + "@" + tenantDomain);
		        stub.updateApplicationDeploymentSuccessStatus(extractAppId(serviceName), extractVersion(serviceName), stage, tenantDomain);

	        } catch (Exception e) {
		        if(log.isDebugEnabled()){
			        log.debug(
					        "Failed to notify the service deployment success event to Appfactory ",
					        e);
		        }
		        log.error("Failed to notify the service deployment success event to Appfactory ");
	        }
        }
    }

    @Override
    public void serviceGroupUpdate(AxisEvent axisEvent, AxisServiceGroup axisServiceGroup) {

    }

    @Override
    public void moduleUpdate(AxisEvent axisEvent, AxisModule axisModule) {

    }

    @Override
    public void addParameter(Parameter parameter) throws AxisFault {

    }

    @Override
    public void removeParameter(Parameter parameter) throws AxisFault {

    }

    @Override
    public void deserializeParameters(OMElement omElement) throws AxisFault {

    }

    @Override
    public Parameter getParameter(String s) {
        return null;
    }

    @Override
    public ArrayList<Parameter> getParameters() {
        return null;
    }

    @Override
    public boolean isParameterLocked(String s) {
        return false;
    }

	/**
	 * Extracts the application ID from the docbase.
	 * @param serviceName
	 * @return appId
	 */
	private String extractAppId(String serviceName) {
		String[] splits = serviceName.split("-");

		String appId = null;
		if (splits.length > 0) {
			appId = splits[0];

		} else {
			appId = serviceName;
		}
		return appId;

	}

	/**
	 * Extracts version from context
	 * @param serviceName
	 * @return
	 */
	private String extractVersion(String serviceName) throws AppFactoryException {
		String [] splits = serviceName.split("-");
		String artifactVersionName = AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty("TrunkVersioning.ServiceVersioning.ArtifactVersionName");
		String sourceVersionName = AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty("TrunkVersioning.ServiceVersioning.SourceVersionName");

		String version = null;
		if (splits.length > 1){
			version = splits[splits.length -1];
		}

		if (version != null && artifactVersionName != null && version.equalsIgnoreCase(artifactVersionName)){
			return sourceVersionName;
		}

		return version;
	}
}


