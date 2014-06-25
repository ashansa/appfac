package org.wso2.carbon.appfactory.s4.integration.cloud;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.s4.integration.DomainMapperEventHandler;
import org.wso2.carbon.appfactory.s4.integration.utils.CloudUtils;

public class AWSRoute53DomainNameService implements DomainMapperEventHandler {
	private static final Log log = LogFactory.getLog(AWSRoute53DomainNameService.class);

	@Override
	public void onDomainMappingCreate(String domain) throws AppFactoryException {
		if (CloudUtils.isAWSRoute53Enabled()) {
			try {
				CloudUtils.sendRequest(CloudUtils.prepareCNAMERecordsReq("CREATE",
				                                                         CloudUtils.getLBUrl(),
				                                                         domain));
			} catch (AppFactoryException e) {
				String msg = "Error occured while creating CNAME records for domains " + domain;
				log.error(msg, e);
				throw new AppFactoryException(msg, e);
			}
		}

	}

	@Override
	public void OnDomainMappingDelete(String domain) throws AppFactoryException {
		if (CloudUtils.isAWSRoute53Enabled()) {
			try {
				CloudUtils.sendRequest(CloudUtils.prepareCNAMERecordsReq("DELETE",
				                                                         CloudUtils.getLBUrl(),
				                                                         domain));
			} catch (AppFactoryException e) {
				String msg = "Error occured deleting CNAME records for domains " + domain;
				log.error(msg, e);
				throw new AppFactoryException(msg, e);
			}
		}
	}

}
