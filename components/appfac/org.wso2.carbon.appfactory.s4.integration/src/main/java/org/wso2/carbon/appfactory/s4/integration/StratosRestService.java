package org.wso2.carbon.appfactory.s4.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.stratos.cli.beans.SubscriptionInfo;
import org.apache.stratos.cli.beans.TenantInfoBean;
import org.apache.stratos.cli.beans.cartridge.CartridgeInfoBean;
import org.apache.stratos.cli.exception.CommandException;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.s4.integration.utils.StratosResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Http Client based REST Client for calling Stratos APIs
 */
public class StratosRestService {

	private static final Log log = LogFactory.getLog(StratosRestService.class);

	private String stratosManagerURL;
	private String username;
	private String password;
	// REST endpoints
	private final String initializeEndpoint = "/stratos/admin/init";
	private final String listAvailableCartridgesRestEndpoint = "/stratos/admin/cartridge/list";
	private final String listSubscribedCartridgesRestEndpoint =
	                                                            "/stratos/admin/cartridge/list/subscribed";
	private final String subscribCartridgeRestEndpoint = "/stratos/admin/cartridge/subscribe";
	private final String addTenantEndPoint = "/stratos/admin/tenant";
	private final String unsubscribeTenantEndPoint = "/stratos/admin/cartridge/unsubscribe";
	private final String cartridgeDeploymentEndPoint = "/stratos/admin/cartridge/definition";
	private final String partitionDeploymentEndPoint = "/stratos/admin/policy/deployment/partition";
	private final String autoscalingPolicyDeploymentEndPoint = "/stratos/admin/policy/autoscale";
	private final String deploymentPolicyDeploymentEndPoint = "/stratos/admin/policy/deployment";
	private final String listParitionRestEndPoint = "/stratos/admin/partition";
	private final String listAutoscalePolicyRestEndPoint = "/stratos/admin/policy/autoscale";
	private final String listDeploymentPolicyRestEndPoint = "/stratos/admin/policy/deployment";
	private HttpClient httpClient;

	public StratosRestService(String stratosManagerURL, String username, String password) {
		this.username = username;
		this.stratosManagerURL = stratosManagerURL;
		this.password = password;
	}

	// This method does the cartridge subscription
	public void subscribe(String cartridgeType, String alias, String externalRepoURL,
	                      boolean privateRepo, String username, String password,
	                      String dataCartridgeType, String dataCartridgeAlias, String asPolicy,
	                      String depPolicy) throws AppFactoryException {

		CartridgeInfoBean cartridgeInfoBean = new CartridgeInfoBean();

		try {

			if (StringUtils.isNotBlank(dataCartridgeType) &&
			    StringUtils.isNotBlank(dataCartridgeAlias)) {
				log.info(String.format("Subscribing to data cartridge %s with alias %s.%n",
				                       dataCartridgeType, dataCartridgeAlias));

				cartridgeInfoBean.setCartridgeType(dataCartridgeType);
				cartridgeInfoBean.setAlias(dataCartridgeAlias);
				cartridgeInfoBean.setRepoURL(null);
				cartridgeInfoBean.setPrivateRepo(false);
				cartridgeInfoBean.setRepoUsername(null);
				cartridgeInfoBean.setRepoPassword(null);
				cartridgeInfoBean.setAutoscalePolicy(null);
				cartridgeInfoBean.setDeploymentPolicy(null);
				subscribeCartridge(cartridgeInfoBean);

			}

			cartridgeInfoBean.setCartridgeType(cartridgeType);
			cartridgeInfoBean.setAlias(alias);
			cartridgeInfoBean.setRepoURL(externalRepoURL);
			cartridgeInfoBean.setPrivateRepo(privateRepo);
			cartridgeInfoBean.setRepoUsername(username);
			cartridgeInfoBean.setRepoPassword(password);
			cartridgeInfoBean.setAutoscalePolicy(asPolicy);
			cartridgeInfoBean.setDeploymentPolicy(depPolicy);
			subscribeCartridge(cartridgeInfoBean);

		} catch (Exception e) {
			handleException("Exception in subscribing to cartridge", e);
		}
	}

	private void subscribeCartridge(CartridgeInfoBean cartridgeInfoBean) throws CommandException,
	                                                                    AppFactoryException {
		String completeJsonSubscribeString;

		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		completeJsonSubscribeString = gson.toJson(cartridgeInfoBean, CartridgeInfoBean.class);

		HttpClient httpClient = getNewHttpClient();

		StratosResponse response =
		                        doPost(httpClient, this.stratosManagerURL +
		                                           this.subscribCartridgeRestEndpoint,
		                               completeJsonSubscribeString);

		if (response.getStatusCode() == HttpStatus.SC_OK) {

			if (log.isDebugEnabled()) {
				log.debug(" Status 200 returned when subsctibing to cartridge " +
				          cartridgeInfoBean.getCartridgeType());
			}

			String subscriptionOutput = response.getResponse();

			if (subscriptionOutput == null) {
				log.error("Error occurred while getting response. Response is null");
				return;
			}

			String subscriptionOutputJSON =
			                                subscriptionOutput.substring(20,
			                                                             subscriptionOutput.length() - 1);
			SubscriptionInfo subcriptionInfo =
			                                   gson.fromJson(subscriptionOutputJSON,
			                                                 SubscriptionInfo.class);

			log.info(String.format("Successfully subscribed to %s cartridge with alias %s with repo url %s"
			                               + ".%n", cartridgeInfoBean.getCartridgeType(),
			                       cartridgeInfoBean.getAlias(), subcriptionInfo.getRepositoryURL()));
		} else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
			log.error("Authorization failed when subsctibing to cartridge " +
			          cartridgeInfoBean.getCartridgeType());
			return;
		} else {
			log.error("Error occurred while subscribing to cartdridge," + "server returned  " +
			          response.getStatusCode() + " " + response.getResponse());
			return;
		}
	}

	// This method helps to create the new tenant
	public void addTenant(String admin, String firstName, String lastName, String password,
	                      String domain, String email) throws AppFactoryException {
		HttpClient httpClient = getNewHttpClient();
		try {
			TenantInfoBean tenantInfo = new TenantInfoBean();
			tenantInfo.setAdmin(admin);
			tenantInfo.setFirstname(firstName);
			tenantInfo.setLastname(lastName);
			tenantInfo.setAdminPassword(password);
			tenantInfo.setTenantDomain(domain);
			tenantInfo.setEmail(email);

			GsonBuilder gsonBuilder = new GsonBuilder();
			Gson gson = gsonBuilder.create();

			String jsonString = gson.toJson(tenantInfo, TenantInfoBean.class);
			String completeJsonString = "{\"tenantInfoBean\":" + jsonString + "}";

			StratosResponse response =
			                        doPost(httpClient, this.stratosManagerURL + addTenantEndPoint,
			                               completeJsonString);

			if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
				log.error("Authorization failed for the operation");
				return;
			} else if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
				log.debug("Tenant added successfully");
				return;
			} else if (response.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
				log.error("Error occurred while adding tenant," + "server returned  " +
				          response.getStatusCode());
				return;
			} else {
				System.out.println("Unhandle error");
				return;
			}

		} catch (Exception e) {
			log.error(e);
			handleException("Exception in creating tenant", e);
		}
	}

	// This method helps to unsubscribe cartridges
	public void unsubscribe(String alias) throws AppFactoryException {
		HttpClient httpClient = getNewHttpClient();
		try {
			doPost(httpClient, this.stratosManagerURL + unsubscribeTenantEndPoint, alias);
			log.info("Successfully unsubscribed " + alias);
		} catch (Exception e) {
			handleException("Exception in un-subscribing cartridge", e);
		}
	}

	// This method gives the HTTP response string
	private String getHttpResponseString(HttpResponse response) {
		try {
			BufferedReader reader =
			                        new BufferedReader(
			                                           new InputStreamReader(
			                                                                 (response.getEntity().getContent())));

			String output;
			String result = "";
			while ((output = reader.readLine()) != null) {
				result += output;
			}
			return result;
		} catch (SocketException e) {
			log.error("Error while connecting to the server ", e);
			return null;
		} catch (NullPointerException e) {
			log.error("Null value return from server ", e);
			return null;
		} catch (IOException e) {
			log.error("IO error ", e);
			return null;
		}
	}

	// This is for handle exception
	private void handleException(String msg, Exception e) throws AppFactoryException {

		log.error(msg, e);
		throw new AppFactoryException(msg, e);
	}

	public StratosResponse doPost(HttpClient httpClient, String resourcePath, String jsonParamString)
	                                                                                              throws CommandException,
	                                                                                              AppFactoryException {

		PostMethod postRequest = new PostMethod(resourcePath);

		StringRequestEntity input = null;
		try {
			input = new StringRequestEntity(jsonParamString, "application/json", "UTF-8");
		} catch (UnsupportedEncodingException e) {
			handleException("Error occurred while getting POST parameters", e);
		}

		postRequest.setRequestEntity(input);

		String userPass = this.username + ":" + this.password;
		String basicAuth = null;
		try {
			basicAuth =
			            "Basic " +
			                    javax.xml.bind.DatatypeConverter.printBase64Binary(userPass.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			handleException("Error occurred while getting username:password", e);
		}
		postRequest.addRequestHeader("Authorization", basicAuth);

		int response = 0;
		String responseString = null;
		try {
			response = httpClient.executeMethod(postRequest);
		} catch (IOException e) {
			handleException("Error occurred while executing POST method", e);
		}
		try {
			responseString = postRequest.getResponseBodyAsString();
		} catch (IOException e) {
			handleException("error while getting response as String", e);
		}

		return new StratosResponse(responseString, response);

	}

	public HttpClient getNewHttpClient() {
		return new HttpClient(new MultiThreadedHttpConnectionManager());
	}
}
