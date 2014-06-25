package org.wso2.carbon.appfactory.eventing.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.jms.AndesAdminServiceClient;
import org.wso2.carbon.context.CarbonContext;

public class Util {
    private static Log log = LogFactory.getLog(Util.class);

    public static String getTCPConnectionURL() throws AppFactoryEventException {
        String currentUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (tenantDomain != null && !tenantDomain.equals(EventingConstants.CARBON_SUPER)) {
            currentUser = currentUser + "@" + tenantDomain;
        }
        try {
            AppFactoryConfiguration appFactoryConfiguration = AppFactoryUtil.getAppfactoryConfiguration();
            String messageBrokerServerUrl = appFactoryConfiguration.getFirstProperty(EventingConstants.NOTIFICATION_SERVER_URL);
            AndesAdminServiceClient client = new AndesAdminServiceClient(messageBrokerServerUrl);
            client.setMutualAuthHeader(currentUser);
            String accessToken = client.getAccessToken();
            String connectionUrl = appFactoryConfiguration.getFirstProperty(EventingConstants.TCP_CONNECTION_URL);
            connectionUrl = connectionUrl.replace(EventingConstants.CONNECTION_USER, getCurrentUser()).
                    replace(EventingConstants.ACCESS_TOKEN, accessToken);
            return connectionUrl;
        } catch (AppFactoryException e) {
            String error = "Failed to get tcp connection URL to message broker due to " + e.getMessage();
            log.error(error, e);
            throw new AppFactoryEventException(error, e);
        }
    }

    private static String getCurrentUser(){
        String userName = "";
        if (CarbonContext.getThreadLocalCarbonContext().getTenantId() != 0) {
            userName = CarbonContext.getThreadLocalCarbonContext().getUsername() + "!"
                    + CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        } else {
            userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
        }
        return userName.trim();
    }

    public static String getUniqueSubscriptionId(String topic, String subscriberId) {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        // in tenant mode, subscriptionId should start with tenant domain.
        return tenantDomain + "/" + topic.concat("_").concat(subscriberId);
    }

}
