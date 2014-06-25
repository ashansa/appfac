package org.wso2.carbon.appfactory.eventing.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.social.core.service.SocialActivityService;

/**
 * @scr.component name="org.wso2.carbon.appfactory.eventing.internal"
 * immediate="true"
 * @scr.reference name="org.wso2.carbon.social.component" interface="org.wso2.carbon.social.core.service.SocialActivityService"
 * cardinality="0..1" policy="dynamic"  bind="setSocialActivityService" unbind="unsetSocialActivityService"
 */
public class AppFactoryEventingDS {
    private static final Log log = LogFactory.getLog(AppFactoryEventingDS.class);

    @SuppressWarnings("UnusedDeclaration")
    protected void activate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("appfactory.eventing service bundle is activated");
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("appfactory.eventing service bundle is deactivated");
        }
    }

    protected void setSocialActivityService(SocialActivityService socialActivityService) {
        ServiceHolder.setSocialActivityService(socialActivityService);
    }

    protected void unsetSocialActivityService(SocialActivityService socialActivityService) {
        ServiceHolder.setSocialActivityService(null);
    }


}
