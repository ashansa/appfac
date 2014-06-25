package org.wso2.carbon.appfactory.ext.authorization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.ext.internal.ServiceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.authorization.AuthorizationCache;

public class AuthorizationCacheInvalidationService {
    private static Log log = LogFactory.getLog(AuthorizationCacheInvalidationService.class);

    public void invalidateCache() throws AppFactoryException {
        if (isInvalidationAllowed()) {
            AuthorizationCache authorizationCache = AuthorizationCache.getInstance();
            if (authorizationCache != null) {
                authorizationCache.clearCacheByTenant(CarbonContext.getThreadLocalCarbonContext().getTenantId());
                if (log.isDebugEnabled()) {
                    log.debug("Authorization cache is invalidated successfully.");
                }
            } else {
                log.warn("Authorization cache is null, cache invalidation is ignored.");
            }
        } else {
            log.warn(CarbonContext.getThreadLocalCarbonContext().getUsername() + " is trying to invalidate authorization cache.");
        }
    }

    private boolean isInvalidationAllowed() throws AppFactoryException {
        String currentCloudStage = System.getProperty(AppFactoryConstants.CLOUD_STAGE);
        if (currentCloudStage == null || currentCloudStage.isEmpty()) {
            log.error(AppFactoryConstants.CLOUD_STAGE + " system variable is not set.");
            throw new AppFactoryException(AppFactoryConstants.CLOUD_STAGE + " system variable is not set.");
        }
        String currentUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (currentUser == null) {
            return false;
        }

        try {
            AuthorizationManager authorizationManager = ServiceHolder.getInstance().getRealmService()
                    .getTenantUserRealm(CarbonContext.getThreadLocalCarbonContext().getTenantId())
                    .getAuthorizationManager();
            return authorizationManager.isUserAuthorized(currentUser,
                    AppFactoryConstants.PERMISSION_TENANT_ADMIN,
                    CarbonConstants.UI_PERMISSION_ACTION);

        } catch (UserStoreException e) {
            String errorMsg = "Error occurred while getting authorization manager.";
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }
    }

}
