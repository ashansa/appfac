/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.utilities.application.type;

import org.apache.maven.model.Model;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.appfactory.utilities.version.AppVersionStrategyExecutor;

import java.io.File;
import java.util.List;

/**
 * Data service application type processor
 */
public class DataServiceApplicationTypeProcessor extends AbstractApplicationTypeProcessor {
    @Override
    public void doVersion(String applicationId, String targetVersion, String currentVersion, String workingDirectory) throws AppFactoryException {
        AppVersionStrategyExecutor.doVersionForDBS(targetVersion, new File(workingDirectory));
    }

    @Override
    public void generateApplicationSkeleton(String applicationID, String workingDirectory) throws AppFactoryException {
        ProjectUtils.generateDBSAppArchetype(applicationID, workingDirectory, getProperty(MAVEN_ARCHETYPE_REQUEST));
	    configureFinalName(workingDirectory, applicationID,
	                       AppFactoryUtil.getAppfactoryConfiguration().
			                       getFirstProperty("TrunkVersioning.ServiceVersioning.ArtifactVersionName"));
    }

	public String getDeployedURL(String tenantDomain, String applicationID,
	                             String applicationVersion, String stage) throws AppFactoryException{
		String url = (String) this.properties.get(LAUNCH_URL_PATTERN);

		String artifactTrunkVersionName = AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty("TrunkVersioning.ServiceVersioning.ArtifactVersionName");
		String sourceTrunkVersionName = AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty("TrunkVersioning.ServiceVersioning.SourceVersionName");
		if(applicationVersion.equalsIgnoreCase(sourceTrunkVersionName)) {
			applicationVersion = artifactTrunkVersionName;
		}

		String urlStageValue = "";

		try {
			urlStageValue = (String) this.properties.get(stage + PARAM_APP_STAGE_NAME_SUFFIX);
		} catch (Exception e){}

		if(urlStageValue == null){
			urlStageValue = "";
		}

		url = url.replace(PARAM_TENANT_DOMAIN, tenantDomain).replace(PARAM_APP_ID, applicationID)
		         .replace(PARAM_APP_VERSION, applicationVersion).replace(PARAM_APP_STAGE, urlStageValue);
		return url;
	}

	private void configureFinalName(String path, String appId, String version) {
		File artifactDir = new File(path);
		Model model;
		try {
			String[] fileExtension = {"dbs"};
			List<File> fileList = (List<File>) org.apache.commons.io.FileUtils.listFiles(artifactDir,
			                                                                             fileExtension, true);

			for (File file : fileList) {
				File renamedFile = new File(file.getPath().substring(0, file.getPath().
						lastIndexOf(File.separator) + 1) + appId + "-default-" + version + ".dbs");
				file.renameTo(renamedFile);
			}
		} catch (Exception e) {
			//TODO
			e.printStackTrace();
		}
	}
}
