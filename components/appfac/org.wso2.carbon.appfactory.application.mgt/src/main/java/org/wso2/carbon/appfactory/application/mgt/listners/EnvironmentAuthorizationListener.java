package org.wso2.carbon.appfactory.application.mgt.listners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;
import org.wso2.carbon.appfactory.eventing.EventBuilderUtil;
import org.wso2.carbon.appfactory.eventing.EventNotifier;
import org.wso2.carbon.appfactory.utilities.security.authorization.RemoteAuthorizationMgtClient;
import org.wso2.carbon.user.core.Permission;

import java.util.Map;

/*
  This is to authorize different cloud runtime environments based on application specific events.
 */
public class EnvironmentAuthorizationListener extends ApplicationEventsHandler {

	private static Log log = LogFactory.getLog(EnvironmentAuthorizationListener.class);
    
    public EnvironmentAuthorizationListener(String identifier, int priority) {
    	super(identifier, priority);
    }

    @Override
    public void onCreation(Application application, String userName, String tenantDomain, boolean isUploadableAppType) throws AppFactoryException {
        // authorize application specific unique role in all cloud environments.
        log.info("EnvironmentAuthorizationListener was called for application:"+application.getId() +" creation event.");
        String applicationRoleName = AppFactoryUtil.getRoleNameForApplication(application.getId());
        Permission perAppRolePermission = new Permission(AppFactoryConstants.PER_APP_ROLE_PERMISSION, CarbonConstants.UI_PERMISSION_ACTION);
        authorizeRole(applicationRoleName, userName, new Permission[]{perAppRolePermission});
        try {
            //
            String infoMessageTitle = "Application " + application.getId() +" is successfully authorized for all " +
                    "Cloud environments";
            String infoMessageBody = "Application " + application.getId() +" is successfully authorized for " +
                    "Development, Test and Production Cloud environments";
            EventNotifier.getInstance().notify(EventBuilderUtil.buildApplicationCreationEvent(infoMessageTitle, infoMessageBody, Event.Category.INFO));
            //EventNotifier.getInstance().notify(EventBuilderUtil.buildApplicationCreationEvent(application.getId(), infoMessage, infoMessage, Event.Category.INFO));
        } catch (AppFactoryEventException e) {
            log.error("Failed to notify Cloud environment authorization events", e);
            // do not throw again.
        }
    }

    @Override
    public void onDeletion(Application application, String userName, String tenantDomain) throws AppFactoryException {
        String applicationRoleName = AppFactoryUtil.getRoleNameForApplication(application.getId());
        clearRoleAuthorization(applicationRoleName, userName);
    }

    @Override
    public void onUserAddition(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        // no remote authorization requirements are identified for this action so far.
    }

    @Override
    public void onUserDeletion(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        // no remote authorization requirements are identified for this action so far.
    }

    @Override
    public void onUserUpdate(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        // no remote authorization requirements are identified for this action so far.
    }

    @Override
    public void onRevoke(Application application, String tenantDomain) throws AppFactoryException {
        // no remote authorization requirements are identified for this action so far.
    }

    @Override
    public void onVersionCreation(Application application, Version source, Version target, String tenantDomain, String userName) throws AppFactoryException {
        // no remote authorization requirements are identified for this action so far.
    }

    @Override
    public void onLifeCycleStageChange(Application application, Version version, String previosStage, String nextStage, String tenantDomain) throws AppFactoryException {
        // no remote authorization requirements are identified for this action so far.
    }

    @Override
    public boolean hasExecuted(Application application, String userName, String tenantDomain) throws AppFactoryException {
        String applicationRoleName = AppFactoryUtil.getRoleNameForApplication(application.getId());
        Permission perAppRolePermission = new Permission(AppFactoryConstants.PER_APP_ROLE_PERMISSION, CarbonConstants.UI_PERMISSION_ACTION);
        return isRoleAuthorized(applicationRoleName, userName,  new Permission[]{perAppRolePermission});
    }

    /**
     * Authorize given role with given set of permissions
     *
     * @param role        - role name
     * @param userName    - authorized user to authorize roles
     * @param permissions - set of permissions
     * @throws AppFactoryException if remote exceptions or user store exceptions occurred.
     */
    private void authorizeRole(String role, String userName, Permission[] permissions) throws AppFactoryException {
        boolean errorOccurred = false;
        // get base access urls from appfactory.xml
        Map<String, String> baseAccessURLs = AppFactoryUtil.getBaseAccessURLs();
        if (baseAccessURLs.isEmpty()) {
            String msg = "Could not find any remote server URLs configured for cloud environments.";
            log.error(msg);
            throw new AppFactoryException(msg);
        }

        for (Map.Entry entry : baseAccessURLs.entrySet()) {
            String stage = (String) entry.getKey();
            try {
                //construct remote service url based on base access url
                String remoteServiceURL = (String) entry.getValue();
                if (!remoteServiceURL.endsWith("/")) {
                    remoteServiceURL += "/services/";
                } else {
                    remoteServiceURL += "services/";
                }

                // create remote authorization management client and authenticate with mutual auth.
                RemoteAuthorizationMgtClient authorizationMgtClient = new RemoteAuthorizationMgtClient(remoteServiceURL);
                authorizationMgtClient.setMutualAuthHeader(userName);

                for (Permission permission : permissions) {
                    try {
                        authorizationMgtClient.authorizeRole(role, permission.getResourceId(), permission.getAction());
                    } catch (Exception e) {
                        String errorMsg = "Failed to authorize role:" + role + " ,permission:" +
                                permission.getResourceId() + " ,action:" + permission.getAction() +
                                " on stage:" + stage;
                        log.error(errorMsg, e);
                        errorOccurred = true;
                        // continue to other permissions and throw generic exception at the end of flow.
                    }
                }
            } catch (Exception e) {
                String errorMsg = "Failed to authorize role:" + role + " on stage:" + stage;
                log.error(errorMsg, e);
                errorOccurred = true;
                // continue to other stages and throw generic exception at the end of flow.
            }
        }

        if (errorOccurred) {
            throw new AppFactoryException("Failed to authorize role:" + role);
        }
    }

    /**
     * Clear the role authorizations from database
     * @param role
     * @param userName
     * @throws AppFactoryException
     */
    private void clearRoleAuthorization(String role, String userName) throws AppFactoryException {
        boolean errorOccurred = false;
        // get base access urls from appfactory.xml
        Map<String, String> baseAccessURLs = AppFactoryUtil.getBaseAccessURLs();
        if (baseAccessURLs.isEmpty()) {
            String msg = "Could not find any remote server URLs configured for cloud environments.";
            log.error(msg);
            throw new AppFactoryException(msg);
        }

        for (Map.Entry entry : baseAccessURLs.entrySet()) {
            String stage = (String) entry.getKey();
            try {
                //construct remote service url based on base access url
                String remoteServiceURL = (String) entry.getValue();
                if (!remoteServiceURL.endsWith("/")) {
                    remoteServiceURL += "/services/";
                } else {
                    remoteServiceURL += "services/";
                }

                // create remote authorization management client and authenticate with mutual auth.
                RemoteAuthorizationMgtClient authorizationMgtClient = new RemoteAuthorizationMgtClient(remoteServiceURL);
                authorizationMgtClient.setMutualAuthHeader(userName);

                    try {
                        authorizationMgtClient.clearAllRoleAuthorization(role);
                    } catch (Exception e) {
                        String errorMsg = "Failed to clear authorization for role:" + role  + " on stage:" + stage;
                        log.error(errorMsg);
                        if (log.isDebugEnabled()) {
                            log.debug(errorMsg, e);
                        }
                        errorOccurred = true;
                        // continue to other permissions and throw generic exception at the end of flow.
                    }

            } catch (Exception e) {
                String errorMsg = "Failed to clear role:" + role + " on stage:" + stage;
                log.error(errorMsg);
                if (log.isDebugEnabled()) {
                    log.debug(errorMsg, e);
                }
                errorOccurred = true;
                // continue to other stages and throw generic exception at the end of flow.
            }
        }

        if (errorOccurred) {
            throw new AppFactoryException("Failed to clear role:" + role);
        }
    }

    /**
     *
     * @param role
     * @param userName
     * @param permissions
     * @return return true if the role has all the permissions given in all the stages
     * @throws AppFactoryException
     */
    private boolean isRoleAuthorized(String role, String userName, Permission[] permissions) throws AppFactoryException {
        boolean roleHasAccess = true;
        // get base access urls from appfactory.xml
        Map<String, String> baseAccessURLs = AppFactoryUtil.getBaseAccessURLs();
        if (baseAccessURLs.isEmpty()) {
            String msg = "Could not find any remote server URLs configured for cloud environments.";
            log.error(msg);
            throw new AppFactoryException(msg);
        }

        for (Map.Entry entry : baseAccessURLs.entrySet()) {
            String stage = (String)entry.getKey();
                try {
                    //construct remote service url based on base access url
                    String remoteServiceURL = (String) entry.getValue();
                    if (!remoteServiceURL.endsWith("/")) {
                        remoteServiceURL += "/services/";
                    } else {
                        remoteServiceURL += "services/";
                    }

                    // create remote authorization management client and authenticate with mutual auth.
                    RemoteAuthorizationMgtClient authorizationMgtClient = new RemoteAuthorizationMgtClient(remoteServiceURL);
                    authorizationMgtClient.setMutualAuthHeader(userName);

                    for (Permission permission : permissions) {
                        try {
                            roleHasAccess = authorizationMgtClient.isRoleAuthorized(role, permission.getResourceId(), permission.getAction());
                            if(!roleHasAccess) {
                                return roleHasAccess;
                            }
                        } catch (Exception e) {
                            String errorMsg = "Failed to authorize role:" + role + " ,permission:" +
                                    permission.getResourceId() + " ,action:" + permission.getAction() +
                                    " on stage:" + stage;
                            log.error(errorMsg, e);
                            roleHasAccess = false;
                            return roleHasAccess;
                            // continue to other permissions and throw generic exception at the end of flow.
                        }
                    }
                } catch (Exception e) {
                    String errorMsg = "Failed to clear role:" + role + " on stage:" + stage;
                    log.error(errorMsg, e);
                    // continue to other stages and throw generic exception at the end of flow.
                }
        }

        return roleHasAccess;
    }

    @Override
	public void onFork(Application application, String userName, String tenantDomain, String version, String[] forkedUsers) throws AppFactoryException {
		// TODO Auto-generated method stub
		
	}
}
