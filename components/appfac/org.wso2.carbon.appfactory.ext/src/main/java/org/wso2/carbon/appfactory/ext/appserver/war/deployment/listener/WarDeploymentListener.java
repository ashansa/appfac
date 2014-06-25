package org.wso2.carbon.appfactory.ext.appserver.war.deployment.listener;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

/**
 * Intercepts life cycle events related to web applications. and notifies Appfactory
 *
 */
public class WarDeploymentListener implements LifecycleListener {

    private static final Log log = LogFactory.getLog(WarDeploymentListener.class);

    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {

        if (!(lifecycleEvent.getSource() instanceof StandardContext)) {
            return;
        }

        StandardContext context = (StandardContext) lifecycleEvent.getSource();

        /*
         * NOTE: Don't try to parse tenant domain from context path. AS
         * periodically fires life cycle events for which carboncontext don't
         * have tenant domain (and tenant ID is
         * MultitenantConstants.INVALID_TENANT_ID). we don't have to care about
         * these life cycle events. What we should look for is events which have
         * tenant domain (and tenant ID is set to valid values)
         */
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
	    String stage= System.getProperty("stratos.stage");

        if (StringUtils.isNotEmpty(tenantDomain)) {
            String appId = extractAppId(context.getPath());
	        String appVersion = null;
	        try {
		        appVersion = extractVersion(context.getPath());
	        } catch (AppFactoryException e) {
		        if(log.isDebugEnabled()){
			        log.debug(
					        "Failed to retrieve the stage. ", e);
		        }
		        log.error("Failed to retrieve the stage.");
	        }
	        handleEvent(tenantDomain, appId, appVersion, stage, lifecycleEvent);

        }

    }

    private void handleEvent(String tenantDomain, String appId, String appVersion, String stage, LifecycleEvent lifecycleEvent) {

        if (log.isDebugEnabled()) {
            log.debug(String.format("life cycle event intercepted:  Tenant Domain = {%s} ,  Application ID = {%s}, Life Cycle State = {%s}",
                                    tenantDomain, appId, lifecycleEvent.getLifecycle().getState()
                                                                       .name()));
        }

        switch (lifecycleEvent.getLifecycle().getState()) {

            case STARTED:
                onApplicationStarted(tenantDomain, appId, appVersion, stage, lifecycleEvent);
                break;
            case FAILED:
                onApplicationFailed(tenantDomain, appId, appVersion, stage, lifecycleEvent);
                break;
            case STOPPED:
                onApplicationStopped(tenantDomain, appId, appVersion, stage, lifecycleEvent);
                break;
            default:
                if (log.isDebugEnabled()) {
                    log.debug(String.format("life cycle event is not sent to appfactory messaging system:  Tenant Domain = {%s} ,  Application ID = {%s}, Application Version = {%s},  Life Cycle State = {%s}",
                                            tenantDomain, appId, appVersion,  lifecycleEvent.getLifecycle().getState().name()));
                }
        }

    }

    private void onApplicationStopped(String tenantDomain, String appId, String appVersion, String stage, LifecycleEvent lifecycleEvent) {
        String msg = "Version " + appVersion + " of " + appId + " undeployed in " + stage + " stage.";
        String msgDesc = "Version " + appVersion + " of Application " + appId + " was undeployed in " + stage + " stage.";
        sendNotification(tenantDomain, appId, lifecycleEvent, msg, msgDesc, Event.Category.INFO);
    }

    private void onApplicationStarted(String tenantDomain, String appId, String appVersion, String stage, LifecycleEvent lifecycleEvent) {
        String msg = "Version " + appVersion + " of " + appId + " deployed in " + stage + " stage.";
        String msgDesc = "Version " + appVersion + " of Application " + appId + " was successfully deployed in " + stage + " stage.";
        sendNotification(tenantDomain, appId, lifecycleEvent, msg, msgDesc, Event.Category.INFO);
	    sendDeploymentStatus(tenantDomain, appId, appVersion, stage, lifecycleEvent);
    }

    private void onApplicationFailed(String tenantDomain, String appId, String appVersion, String stage, LifecycleEvent lifecycleEvent) {
        String msg = "Deployment failed for version " + appVersion + " of " + appId + " in " + stage + " stage.";
        String msgDesc = "Failed to deploy the version " + appVersion + " of application " + appId + " in " + stage + " stage.";
        sendNotification(tenantDomain, appId, lifecycleEvent, msg, msgDesc, Event.Category.ERROR);
    }

    private void sendNotification(String tenantDomain, String appId, LifecycleEvent lifecycleEvent,
                                  String msg, String msgDescription, Event.Category catagory) {
        try {
            EventNotifier.getInstance()
                         .notify(EventBuilderUtil.buildObtainWarDeploymentStatusEvent(appId,
                                                                                      tenantDomain,
                                                                                      msg, msgDescription,
                                                                                      catagory));
        } catch (AppFactoryEventException e) {
            log.error("Failed to notify the Application deployment success event ", e);
        }
    }

	private void sendDeploymentStatus(String tenantDomain, String appId, String version, String stage, LifecycleEvent lifecycleEvent){
		try {
			if (tenantDomain == MultitenantConstants.SUPER_TENANT_DOMAIN_NAME){
				return;
			}

			TenantManager manager = ServiceHolder.getInstance().getRealmService().getTenantManager();
			int tenantId = manager.getTenantId(tenantDomain);
			StandardContext context = (StandardContext) lifecycleEvent.getSource();

			String afUrl = AppFactoryUtil.getAppfactoryConfiguration().
					getFirstProperty("ServerUrls.AppFactory");
			ApplicationManagementServiceStub stub =
					new ApplicationManagementServiceStub(afUrl + "ApplicationManagementService");
			stub._getServiceClient().removeHeaders();
			stub._getServiceClient()
			           .addStringHeader(new QName("http://mutualssl.carbon.wso2.org", "UserName",
			                                      "tns"),
			                            manager.getTenant(tenantId).getAdminName() + "@" +
			                            tenantDomain);


			if(log.isDebugEnabled()){
				log.debug("Notifing deployment success status of appid : " + appId + ", version : " + version + ", tenantDomain : " + tenantDomain);
			}

			stub.updateApplicationDeploymentSuccessStatus(appId, version, stage, tenantDomain);

			if(log.isDebugEnabled()){
				log.debug("Notified deployment success status of appid : " + appId + ", version : " + version + ", tenantDomain : " + tenantDomain);
			}

		} catch (Exception e){
			if(log.isDebugEnabled()){
				log.debug(
						"Failed to notify the Application deployment success event to Appfactory ",
						e);
			}
			log.error("Failed to notify the Application deployment success event to Appfactory ");
		}
	}


    /**
     * Extracts the application ID from the docbase.
     * @param docBase
     * @return appId
     */
	private String extractAppId(String docBase) {
		String baseName = FilenameUtils.getName(docBase);
		String[] splits = baseName.split("-");

		String appId = null;
		if (splits.length > 0) {
			appId = splits[0];

		} else {
			appId = baseName;
		}
		return appId;

	}

	/**
	 * Extracts version from context
	 * @param docBase
	 * @return
	 */
	private String extractVersion(String docBase) throws AppFactoryException {
		String baseName = FilenameUtils.getName(docBase);
		String [] splits = baseName.split("-", 2);
		String artifactVersionName = AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty("TrunkVersioning.WebappVersioning.ArtifactVersionName");
		String sourceVersionName = AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty("TrunkVersioning.WebappVersioning.SourceVersionName");

		String version = null;
		if (splits.length > 1){
			version = splits[1];
		}

		if (version != null && artifactVersionName != null && version.equalsIgnoreCase(artifactVersionName)){
			return sourceVersionName;
		}

		splits = baseName.split("-");
		if (splits.length > 0){
			version = splits[splits.length - 1];
		}

		return version;

	}
}
