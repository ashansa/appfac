package org.wso2.carbon.appfactory.repository.mgt.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;
import org.wso2.carbon.appfactory.eventing.EventBuilderUtil;
import org.wso2.carbon.appfactory.eventing.EventNotifier;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryProvider;
import org.wso2.carbon.appfactory.repository.mgt.internal.Util;

public class RepositoryHandler extends ApplicationEventsHandler {


	private static final Log log = LogFactory.getLog(RepositoryHandler.class);

	public RepositoryHandler(String identifier, int priority) {
		super(identifier, priority);
	}
	
    @Override
    public void onCreation(Application application, String userName,
                           String tenantDomain, boolean isUploadableAppType) throws AppFactoryException {
        String url;
        RepositoryProvider provider = Util.getRepositoryProvider(application.getRepositoryType());
        try {
            provider.createRepository(application.getId(), tenantDomain);
            url = provider.getAppRepositoryURL(application.getId(), tenantDomain);
            provider.getBranchingStrategy().prepareRepository(application.getId(), url, tenantDomain);
            try {
                String infoMessage = "Initial " + application.getRepositoryType().toUpperCase() + " repo created for " +
                        application.getId() + ".";
                String infoMsgDesc =  "Initial " + application.getRepositoryType().toUpperCase() + " source repository creation for application:" +
                        application.getId() + " is successfully completed. Repository is created with URL:" + url;
                EventNotifier.getInstance().notify(EventBuilderUtil.buildApplicationCreationEvent(
                        infoMessage, infoMsgDesc, Event.Category.INFO));
            } catch (AppFactoryEventException e) {
                log.error("Failed to notify Initial source repository creation event", e);
                // do not throw again.
            }
        } catch (RepositoryMgtException e) {
            String error = "Error while preparing repository";
            log.error(error, e);
            throw new AppFactoryException(error, e);
        }
    }

    @Override
    public void onDeletion(Application application, String userName,
                           String tenantDomain) throws AppFactoryException {
        RepositoryProvider provider = Util.getRepositoryProvider(application.getRepositoryType());
        try {
            provider.deleteRepository(application.getId(), tenantDomain);
        } catch (RepositoryMgtException e) {
            String error = "Error while deleting repository";
            log.error(error);
            if (log.isDebugEnabled()) {
                log.debug(error, e);
            }
            throw new AppFactoryException(error, e);
        }
    }

    @Override
    public void onUserAddition(Application application, UserInfo user,
                               String tenantDomain) throws AppFactoryException {
        //Fork the existing repo
        // provider.createFork(repoUrl);

    }

    @Override
    public void onUserDeletion(Application application, UserInfo user,
                               String tenantDomain) throws AppFactoryException {
        //no actions needed
    }

    @Override
    public void onUserUpdate(Application application, UserInfo user,
                             String tenantDomain) throws AppFactoryException {
        //no actions needed
    }

    @Override
    public void onRevoke(Application application, String tenantDomain)
            throws AppFactoryException {
        //no actions needed
    }

    @Override
    public void onVersionCreation(Application application, Version source,
                                  Version target, String tenantDomain, String userName)
            throws AppFactoryException {

    }


    @Override
    public void onLifeCycleStageChange(Application application,
                                       Version version, String previosStage, String nextStage,
                                       String tenantDomain) throws AppFactoryException {
        //no actions needed
    }

    @Override
    public boolean hasExecuted(Application application, String userName, String tenantDomain) throws AppFactoryException {
        return true;
    }

    @Override
    public void onFork(Application application, String userName, String tenantDomain, String version, String[] forkedUsers) throws AppFactoryException {
        // TODO Auto-generated method stub

    }

}
