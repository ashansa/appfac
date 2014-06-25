/*
 * Copyright 2005-2013 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.s4.integration.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.s4.integration.DomainMapperEventHandler;
import org.wso2.carbon.appfactory.s4.integration.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.CarbonContext;

public class DomainMappingUtils {
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final Log log = LogFactory.getLog(DomainMappingUtils.class);

	/**
	 * 
	 * @param stage
	 * @param domains
	 * @param addSubscriptionDomainEndPoint
	 * @throws AppFactoryException
	 */
	public static StratosResponse sendPostRequest(String stage, String body,
	                                              String addSubscriptionDomainEndPoint)
	                                                                                   throws AppFactoryException {

		HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
		String endPointUrl = getSMUrl(stage) + addSubscriptionDomainEndPoint;
		PostMethod postMethod = new PostMethod(endPointUrl);

		// password as garbage value since we authenticate with mutual ssl
		postMethod.setRequestHeader(AUTHORIZATION_HEADER, getAuthHeaderValue());
		StringRequestEntity requestEntity;
		try {
			requestEntity = new StringRequestEntity(body, "application/json", "UTF-8");
		} catch (UnsupportedEncodingException e) {
			String msg = "Error while setting parameters";
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		}
		postMethod.setRequestEntity(requestEntity);

		return send(httpClient, postMethod);
	}

	/**
	 * 
	 * @return
	 */
	private static String getAuthHeaderValue() {
		String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();

		byte[] getUserPasswordInBytes = (userName + ":nopassword").getBytes();
		String encodedValue = new String(Base64.encodeBase64(getUserPasswordInBytes));
		return "Basic " + encodedValue;
	}

	/**
	 * 
	 * @param stage
	 * @param addSubscriptionDomainEndPoint
	 * @throws AppFactoryException
	 */
	public static StratosResponse sendDeleteRequest(String stage,
	                                                String removeSubscriptionDomainEndPoint)
	                                                                                        throws AppFactoryException {

		HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
		String endPointUrl = getSMUrl(stage) + removeSubscriptionDomainEndPoint;
		DeleteMethod deleteMethod = new DeleteMethod(endPointUrl);

		deleteMethod.setRequestHeader(AUTHORIZATION_HEADER, getAuthHeaderValue());

		return send(httpClient, deleteMethod);
	}

	/**
	 * 
	 * @param stage
	 * @param addSubscriptionDomainEndPoint
	 * @throws AppFactoryException
	 */
	public static StratosResponse sendGetRequest(String stage,
	                                             String validateSubscriptionDomainEndPoint)
	                                                                                       throws AppFactoryException {

		HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
		String endPointUrl = getSMUrl(stage) + validateSubscriptionDomainEndPoint;
		GetMethod getMethod = new GetMethod(endPointUrl);

		getMethod.setRequestHeader(AUTHORIZATION_HEADER, getAuthHeaderValue());
		return send(httpClient, getMethod);

	}

	/**
	 * 
	 * @param httpClient
	 * @param method
	 * @throws AppFactoryException
	 */
	private static StratosResponse send(HttpClient httpClient, HttpMethodBase method)
	                                                                                 throws AppFactoryException {
		int responseCode = 0;
		String responseString = null;
		try {
			responseCode = httpClient.executeMethod(method);
		} catch (IOException e) {
			String msg = "Error occurred while executing method " + method.getName();
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		}
		try {
			responseString = method.getResponseBodyAsString();
		} catch (IOException e) {
			String msg = "error while getting response as String for " + method.getName();
			log.error(msg, e);
			throw new AppFactoryException(msg, e);

		} finally {
			method.releaseConnection();
		}
		if (log.isDebugEnabled()) {
			log.debug(" DomainMappingManagementService response id: " + responseCode +
			          " message:O " + responseString);
		}
		return new StratosResponse(responseString, responseCode);

	}

	/**
	 * 
	 * @param domains
	 * @return
	 */
	public static String generateAddSubscriptionDomainJSON(String domain, String appKey,
	                                                       String version) {
		StringBuffer jsonString = new StringBuffer("{\"domains\":{");
		jsonString.append("\"domainName\": \"" + domain + "\",");
		jsonString.append("\"applicationContext\": \"" + "/t/" + CarbonContext.getCurrentContext().getTenantDomain() + "/webapps/" + appKey +
		                  "-" + version + "\"");
		jsonString.append("}}");
		log.debug(" DomainMappingManagementService json string: " + jsonString);
		return jsonString.toString();
	}

	/**
	 * 
	 * @param stage
	 * @return
	 */
	public static String getSMUrl(String stage) {
		return ServiceHolder.getAppFactoryConfiguration()
		                    .getFirstProperty("ApplicationDeployment.DeploymentStage." + stage +
		                                              ".TenantMgtUrl");
	}

	/**
	 * 
	 * @param stage
	 * @return
	 */
	public static String getSubscriptionAlias(String stage) {
		String tenantDomain =
		                      CarbonContext.getCurrentContext().getTenantDomain()
		                                   .replace(".", "dot");
		return ServiceHolder.getAppFactoryConfiguration()
		                    .getFirstProperty("ApplicationDeployment.DeploymentStage." + stage +
		                                              ".Deployer.ApplicationType.*.Properties.Property.alias") +
		       tenantDomain;
	}

	/**
	 * 
	 * @param stage
	 * @return
	 */
	public static String getCartridgeType(String stage) {
		return ServiceHolder.getAppFactoryConfiguration()
		                    .getFirstProperty("ApplicationDeployment.DeploymentStage." + stage +
		                                              ".Deployer.ApplicationType.*.Properties.Property.cartridgeType");
	}

	/**
	 * 
	 * @param domains
	 * @throws AppFactoryException
	 */
	public static void publishToDomainMappingEventHandlers(String domain, DomainMappingAction action)
	                                                                                                 throws AppFactoryException {
		Set<DomainMapperEventHandler> domainEventHandler =
		                                                   ServiceReferenceHolder.getInstance()
		                                                                         .getDomainMapperEventHandler();
		switch (action) {
			case ADD_DOMAIN_MAPPING:
				for (Iterator<DomainMapperEventHandler> iterator = domainEventHandler.iterator(); iterator.hasNext();) {
					DomainMapperEventHandler domainMapperEventHandler =
					                                                    (DomainMapperEventHandler) iterator.next();
					try {
						domainMapperEventHandler.onDomainMappingCreate(domain);
					} catch (AppFactoryException e) {
						String msg =
						             "Error occured invoking domain mapping created event for " +
						                     domain;
						log.error(msg, e);
						throw new AppFactoryException(msg, e);
					}
				}
				break;
			case REMOVE_DOMAIN_MAPPING:
				for (Iterator<DomainMapperEventHandler> iterator = domainEventHandler.iterator(); iterator.hasNext();) {
					DomainMapperEventHandler domainMapperEventHandler =
					                                                    (DomainMapperEventHandler) iterator.next();

					try {
						domainMapperEventHandler.OnDomainMappingDelete(domain);
					} catch (AppFactoryException e) {
						String msg =
						             "Error occured invoking domain mapping removed event for " +
						                     domain;
						log.error(msg, e);
						throw new AppFactoryException(msg, e);
					}
				}
				break;
			default:

		}

	}
}
