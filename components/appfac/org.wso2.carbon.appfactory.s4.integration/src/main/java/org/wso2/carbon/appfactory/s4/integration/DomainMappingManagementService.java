/*
 * Copyright 2005-2014 WSO2, Inc. (http://wso2.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.appfactory.s4.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.s4.integration.utils.DomainMappingAction;
import org.wso2.carbon.appfactory.s4.integration.utils.DomainMappingUtils;
import org.wso2.carbon.appfactory.s4.integration.utils.StratosResponse;

public class DomainMappingManagementService {
	private static final Log log = LogFactory.getLog(DomainMappingManagementService.class);

	/**
	 * Add new domain mapping entry to stratos
	 * 
	 * @param stage
	 * @param domain
	 *            e.g: some.oragnization1.org this doesn't require the
	 *            protocol
	 *            such as http/https
	 * @param version
	 * @param appKey
	 * @throws AppFactoryException
	 */
	public void addSubscriptionDomain(String stage, String domain, String version, String appKey)
	                                                                                             throws AppFactoryException {
		String addSubscriptionDomainEndPoint =
		                                       "/stratos/admin/cartridge/" +
		                                               DomainMappingUtils.getCartridgeType(stage) +
		                                               "/subscription/" +
		                                               DomainMappingUtils.getSubscriptionAlias(stage) +
		                                               "/domains/";
		try {
			String body =
			              DomainMappingUtils.generateAddSubscriptionDomainJSON(domain, appKey,
			                                                                   version);
			StratosResponse response =
			                           DomainMappingUtils.sendPostRequest(stage, body,
			                                                              addSubscriptionDomainEndPoint);
			if (response.statusCode == 400) {
				throw new AppFactoryException(response.getResponse());
			} else if (response.statusCode == 200) {
				DomainMappingUtils.publishToDomainMappingEventHandlers(domain,
				                                                       DomainMappingAction.ADD_DOMAIN_MAPPING);
			}
		} catch (AppFactoryException e) {
			String msg =
			             "Error occured adding domain mappings to appkey " + appKey + " version " +
			                     version + " domain " + domain;
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		}

	}

	/**
	 * Remove existing domain mapping entries from stratos
	 * 
	 * @param username
	 * @param stage
	 * @param domain
	 *            e.g: some.oragnization1.org this doesn't require the
	 *            protocol
	 *            such as http/https
	 * @throws AppFactoryException
	 */
	public void removeSubscriptionDomain(String stage, String domain) throws AppFactoryException {

		String removeSubscriptionDomainEndPoint =
		                                          "/stratos/admin/cartridge/" +
		                                                  DomainMappingUtils.getCartridgeType(stage) +
		                                                  "/subscription/" +
		                                                  DomainMappingUtils.getSubscriptionAlias(stage) +
		                                                  "/domains/" + domain;
		try {
			DomainMappingUtils.sendDeleteRequest(stage, removeSubscriptionDomainEndPoint);
			DomainMappingUtils.publishToDomainMappingEventHandlers(domain,
			                                                       DomainMappingAction.REMOVE_DOMAIN_MAPPING);
		} catch (AppFactoryException e) {
			String msg = "Error occured removing domain mapping for " + domain;
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		}

	}

}
