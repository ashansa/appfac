/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.git.repository.provider;

import com.gitblit.Constants;
import com.gitblit.GitBlit;
import com.gitblit.models.RepositoryModel;
import com.gitblit.models.UserModel;
import com.gitblit.utils.RpcUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.scm.provider.git.util.GitUtil;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.provider.common.AbstractRepositoryProvider;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * GITBlit specific repository manager implementation for git
 */
public class GITBlitBasedGITRepositoryProvider extends AbstractRepositoryProvider {
	private static final Log log = LogFactory.getLog(GITBlitBasedGITRepositoryProvider.class);

	public static final String BASE_URL = "RepositoryProviderConfig.git.Property.BaseURL";
	public static final String GITBLIT_ADMIN_USERNAME =
	                                                    "RepositoryProviderConfig.git.Property.AdminUserName";
	public static final String GITBLIT_ADMIN_PASS =
	                                                "RepositoryProviderConfig.git.Property.AdminPassword";
	public static final String REPO_TYPE = "git";

	private boolean isCreated = true;
	private boolean isDeleted = true;
	private boolean isForked = true;

	public static final String TYPE = "git";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String createRepository(String applicationKey, String tenantDomain)
	                                                                          throws RepositoryMgtException {
		String repoName = tenantDomain + "/" + applicationKey + ".git";
		String repoCreateUrl = config.getFirstProperty(BASE_URL);
		String adminUsername = config.getFirstProperty(GITBLIT_ADMIN_USERNAME);
		String adminPassword = config.getFirstProperty(GITBLIT_ADMIN_PASS);
		// Create the gitblit repository model
		RepositoryModel model = new RepositoryModel();
		model.name = repoName;
		// authenticated users can clone, push and view the repository
		model.accessRestriction = Constants.AccessRestrictionType.VIEW;
		model.isBare = true; // TODO: temporaryly added for demo purpose, need
							 // to fixed with new gitblit
		try {
			isCreated =
			            RpcUtils.createRepository(model, repoCreateUrl, adminUsername,
			                                      adminPassword.toCharArray());
			if (isCreated) {
				String url = getAppRepositoryURL(applicationKey, tenantDomain);
				return url;
			} else {
				String msg =
				             "Repository is not created for " + applicationKey +
				                     " due to remote server error";
				log.error(msg);
				throw new RepositoryMgtException(msg);
			}
		} catch (IOException e) {
			String msg =
			             "Repository is not created for " + applicationKey + " due to " +
			                     e.getLocalizedMessage();
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		}

	}

	@Override
	public boolean deleteRepository(String applicationKey, String tenantDomain)
	                                                                           throws RepositoryMgtException {
		CarbonContext ct = CarbonContext.getThreadLocalCarbonContext();
		String repoName = tenantDomain + "/" + applicationKey + ".git";
		String repoUrl = config.getFirstProperty(BASE_URL);
		String adminUsername = config.getFirstProperty(GITBLIT_ADMIN_USERNAME);
		String adminPassword = config.getFirstProperty(GITBLIT_ADMIN_PASS);
		// Create the gftblit repository model
		RepositoryModel model = new RepositoryModel();
		model.name = repoName;
		// authenticated users can clone, push and view the repository
		model.accessRestriction = Constants.AccessRestrictionType.VIEW;
		model.isBare = true; // TODO: temporaryly added for demo purpose, need
							 // to fixed with new gitblit
		try {
			RepositoryModel retrievedRepo =
			                                findRepository(model.name, repoUrl, adminUsername,
			                                               adminPassword);
			isDeleted =
			            RpcUtils.deleteRepository(retrievedRepo, repoUrl, adminUsername,
			                                      adminPassword.toCharArray());
		} catch (IOException e) {
			String msg =
			             "Repository is not deleted for " + applicationKey + " due to " +
			                     e.getLocalizedMessage();
			log.error(msg);
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
			throw new RepositoryMgtException(msg, e);
		}
		return isDeleted;
	}

	@Override
	public boolean repoExists(String applicationKey, String tenantDomain)
	                                                                     throws RepositoryMgtException {
		// TODO implement method
		RepositoryModel retrievedRepo = null;
		CarbonContext ct = CarbonContext.getThreadLocalCarbonContext();
		String repoName = tenantDomain + "/" + applicationKey + ".git";
		String repoUrl = config.getFirstProperty(BASE_URL);
		String adminUsername = config.getFirstProperty(GITBLIT_ADMIN_USERNAME);
		String adminPassword = config.getFirstProperty(GITBLIT_ADMIN_PASS);
		// Create the gftblit repository model
		RepositoryModel model = new RepositoryModel();
		model.name = repoName;
		// authenticated users can clone, push and view the repository
		model.accessRestriction = Constants.AccessRestrictionType.VIEW;
		model.isBare = true; // TODO: temporaryly added for demo purpose, need
							 // to fixed with new gitblit
		try {
			retrievedRepo = findRepository(model.name, repoUrl, adminUsername, adminPassword);
		} catch (IOException e) {
			String msg =
			             "Repository is not deleted for " + applicationKey + " due to " +
			                     e.getLocalizedMessage();
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		}
		return retrievedRepo != null;
	}

	private RepositoryModel findRepository(String name, String url, String account, String password)
	                                                                                                throws IOException {
		Map<String, RepositoryModel> repositories =
		                                            RpcUtils.getRepositories(url, account,
		                                                                     password.toCharArray());
		RepositoryModel retrievedRepository = null;
		for (RepositoryModel model : repositories.values()) {
			if (model.name.equalsIgnoreCase(name)) {
				retrievedRepository = model;
				break;
			}
		}
		return retrievedRepository;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAppRepositoryURL(String applicationKey, String tenantDomain)
	                                                                             throws RepositoryMgtException {
		return config.getFirstProperty(BASE_URL) + REPO_TYPE + "/" + tenantDomain + "/" +
		       applicationKey + ".git";
	}

	public void createFork(String repoUrl) {
		RepositoryModel repositoryModel =
		                                  new RepositoryModel("repo2", "repo2 description",
		                                                      "admin", null);
		// RepositoryModel repositoryModel = new RepositoryModel("repo1",
		// "repo1 description", "admin", null);

		UserModel userModel = new UserModel("user2");

		Boolean isforked = null;
		try {
			isforked =
			           RpcUtils.forkRpository(repositoryModel, userModel, repoUrl, "admin",
			                                  "admin".toCharArray());
		} catch (IOException e) {
			e.printStackTrace(); // To change body of catch statement use File |
								 // Settings | File Templates.
		}
		System.out.println("Is Forked: " + isforked);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getType() {
		return TYPE;
	}

	public boolean createTenantRepo(String tenantId) throws RepositoryMgtException {
		String defaultTenantRepo = tenantId + "/defApp";
		String repoCreateUrl =
		                       config.getFirstProperty(BASE_URL) +
		                               "rpc?req=CREATE_REPOSITORY&name=/" + defaultTenantRepo;
		String repoDeleteUrl =
		                       config.getFirstProperty(BASE_URL) +
		                               "rpc?req=DELETE_REPOSITORY&name=/" + defaultTenantRepo;
		String adminUsername = config.getFirstProperty(GITBLIT_ADMIN_USERNAME);
		String adminPassword = config.getFirstProperty(GITBLIT_ADMIN_PASS);

		// Create the gitblit repository model
		RepositoryModel model = new RepositoryModel();
		model.name = defaultTenantRepo;
		// authenticated users can clone, push and view the repository
		model.accessRestriction = Constants.AccessRestrictionType.VIEW;
		try {
			isCreated =
			            RpcUtils.createRepository(model, repoCreateUrl, adminUsername,
			                                      adminPassword.toCharArray());

			if (isCreated) {
				// String url = getAppRepositoryURL(defaultTenantRepo);
				RpcUtils.deleteRepository(model, repoDeleteUrl, adminUsername,
				                          adminPassword.toCharArray());

				return true;
			} else {
				String msg =
				             "Tenant Repsitory is not created for " + tenantId +
				                     " due to remote server error";
				log.error(msg);
				throw new RepositoryMgtException(msg);

			}
		} catch (IOException e) {
			String msg =
			             "Tenant Repsitory is not created for " + tenantId + " due to " +
			                     e.getLocalizedMessage();
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		}

	}

	@Override
	public boolean deleteTenantRepo(String tenantId) throws RepositoryMgtException {
		String defaultTenantRepo = tenantId + "/defApp";
		String repoDeleteUrl =
		                       config.getFirstProperty(BASE_URL) +
		                               "rpc?req=DELETE_REPOSITORY&name=/" + defaultTenantRepo;
		String adminUsername = config.getFirstProperty(GITBLIT_ADMIN_USERNAME);
		String adminPassword = config.getFirstProperty(GITBLIT_ADMIN_PASS);
		// Create the gitblit repository model
		RepositoryModel model = new RepositoryModel();
		model.name = defaultTenantRepo;
		// authenticated users can clone, push and view the repository
		model.accessRestriction = Constants.AccessRestrictionType.VIEW;
		try {
			boolean isDeleted =
			                    RpcUtils.deleteRepository(model, repoDeleteUrl, adminUsername,
			                                              adminPassword.toCharArray());
			if (!isDeleted) {
				log.info("Tenant Repsitory is not deleted for " + tenantId);
			}
			return isDeleted;
		} catch (IOException e) {
			String msg =
			             "Tenant Repsitory is not deleted for " + tenantId + " due to " +
			                     e.getLocalizedMessage();
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		}
	}

	/**
	 * Creating a fork repository.
	 * 
	 */
	@Override
	public String createForkRepo(String applicationKey, String userName)
	                                                                    throws RepositoryMgtException {

		CarbonContext threadLocalCarbonContext = CarbonContext.getThreadLocalCarbonContext();
		String domainName = threadLocalCarbonContext.getTenantDomain();

		// Creating the repository model
		RepositoryModel repositoryModel = new RepositoryModel();
		repositoryModel.name = domainName + "/" + applicationKey + ".git";

		// Creating the user model
		UserModel userModel = new UserModel(userName);
		String userId = MultitenantUtils.getTenantAwareUsername(userName);
		userModel.username = domainName + "/" + userId;

		String adminUsername = config.getFirstProperty(GITBLIT_ADMIN_USERNAME);
		String adminPassword = config.getFirstProperty(GITBLIT_ADMIN_PASS);
		String gitBaseUrl = config.getFirstProperty(BASE_URL);
		try {
			isForked =
			           RpcUtils.forkRpository(repositoryModel, userModel, gitBaseUrl,
			                                  adminUsername, adminPassword.toCharArray());
			if (isForked) {

				String url = getForkedAppRepositoryURL(applicationKey, domainName, userId);

				// the default restriction type is authenticated PUSH.
				// Changing the restriction type to authenticated VIEW, CLONE
				// and PUSH
				String repoName = url.split("/git/")[1];
				changeAccessRestriction(repoName, gitBaseUrl, adminUsername, adminPassword);
				return url;
			} else {
				String msg =
				             "Forked repository was not created for " + userName +
				                     " due to remote server error";
				log.error(msg);
				throw new RepositoryMgtException(msg);
			}
		} catch (IOException e) {
			String msg =
			             "Forked repository was not created for " + userName + " due to " +
			                     e.getLocalizedMessage();
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		}
	}

	@Override
	public String getForkedAppRepositoryURL(String applicationKey, String tenantDomain,
	                                        String userId) throws RepositoryMgtException {
		return config.getFirstProperty(BASE_URL) + REPO_TYPE + "/~" + tenantDomain + "/" + userId +
		       "/" + applicationKey + ".git";
	}

	public void changeAccessRestriction(String repositoryName, String gitBaseUrl,
	                                    String adminUserName, String adminPassword)
	                                                                               throws RepositoryMgtException {
		RepositoryModel repositoryModel = new RepositoryModel();
		repositoryModel.name = repositoryName;
		repositoryModel.accessRestriction = Constants.AccessRestrictionType.VIEW;

		try {
			RpcUtils.updateRepository(repositoryName, repositoryModel, gitBaseUrl, adminUserName,
			                          adminPassword.toCharArray());
		} catch (IOException e) {
			String msg =
			             "Error while updating the repository " + repositoryName + " due to " +
			                     e.getLocalizedMessage();
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		}
	}

	/**
	 * Implementation of forkBranch is used to add new branches to the existing forked repository. This use JGit client to execute composite of commands. 
	 * 
	 */
	public void forkBranch(String applicationKey, String userName, String branch)
	                                                                             throws RepositoryMgtException {

		File file = null;
		try {
			
			CarbonContext threadLocalCarbonContext = CarbonContext.getThreadLocalCarbonContext();
			String domainName = threadLocalCarbonContext.getTenantDomain();

			String adminUsername = config.getFirstProperty(GITBLIT_ADMIN_USERNAME);
			String adminPassword = config.getFirstProperty(GITBLIT_ADMIN_PASS);

			String userId = MultitenantUtils.getTenantAwareUsername(userName);

			String forkedRepoURL = getForkedAppRepositoryURL(applicationKey, domainName, userId);
			String mainRepoURL = getAppRepositoryURL(applicationKey, domainName);

			FileRepositoryBuilder builder = new FileRepositoryBuilder();

			String appFacHome = System.getProperty("carbon.home");
			
			//Creating temporary  path to get clone meta data to appfactory side 
			file =
			       new File(appFacHome + File.separator + "tmpfork" + File.separator + domainName +
			                File.separator + userId + File.separator + applicationKey +
			                File.separator + branch);

			Repository repository = builder.setGitDir(file).readEnvironment().findGitDir().build();
			Git git = new Git(repository);
			CloneCommand clone = git.cloneRepository();
			clone.setBare(false);
			clone.setCloneAllBranches(true);
			clone.setNoCheckout(true);
			clone.setDirectory(file).setURI(forkedRepoURL);
			UsernamePasswordCredentialsProvider user =
			                                           new UsernamePasswordCredentialsProvider(
			                                                                                   adminUsername,
			                                                                                   adminPassword);
			clone.setCredentialsProvider(user);
			Git g = clone.call();

			FetchCommand command = g.fetch();
			command.setRemote(mainRepoURL);
			command.setRefSpecs(new RefSpec("+refs/heads/" + branch));

			command.setCredentialsProvider(user);
			command.call();

			CreateBranchCommand bc = g.branchCreate();
			bc.setName(branch);
			bc.setStartPoint("FETCH_HEAD");
			bc.call();

			PushCommand pushCommand = g.push();
			pushCommand.setRemote(forkedRepoURL);
			pushCommand.add(branch);
			pushCommand.setCredentialsProvider(user);
			pushCommand.call();

		} catch (InvalidRemoteException e) {
			String msg =
			             "Error while forking the repository branch : " + branch + " due to " +
			                     e.getMessage() + " from InvalidRemoteException";
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		} catch (TransportException e) {
			String msg =
			             "Error while forking the repository branch : " + branch + " due to " +
			                     e.getMessage() + " from TransportException";
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		} catch (RefAlreadyExistsException e) {
			String msg =
			             "Error while forking the repository branch : " + branch + " due to " +
			                     e.getMessage() + " from InvalidRemoteException";
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		} catch (RefNotFoundException e) {
			String msg =
			             "Error while forking the repository branch : " + branch + " due to " +
			                     e.getMessage() + " from RefNotFoundException";
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		} catch (InvalidRefNameException e) {
			String msg =
			             "Error while forking the repository branch : " + branch + " due to " +
			                     e.getMessage() + " from InvalidRefNameException";
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		} catch (IOException e) {
			String msg =
			             "Error while forking the repository branch : " + branch + " due to " +
			                     e.getMessage() + " from IOException";
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		} catch (GitAPIException e) {
			String msg =
			             "Error while forking the repository branch : " + branch + " due to " +
			                     e.getMessage() + " from GitAPIException";
			log.error(msg, e);
			throw new RepositoryMgtException(msg, e);
		} finally {
			try {
				FileUtils.deleteDirectory(file);
			} catch (IOException e) {
				log.error("Error when deleting files", e);
				try {
					try {
						log.warn("Sleeping for the moment to delete tmp files...");
						Thread.sleep(5000);
					} catch (InterruptedException e1) {

					}
					FileUtils.deleteDirectory(file);
				} catch (IOException e1) {
					log.error("Error when deleting files secondly", e);
				}
			}
		}

	}

}
