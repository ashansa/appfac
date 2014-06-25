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

import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.ApplicationTypeProcessor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Base class contains getters and setters
 */
public abstract class AbstractApplicationTypeProcessor implements ApplicationTypeProcessor {
    public static final String MAVEN_ARCHETYPE_REQUEST = "MavenArcheTypeRequest";
	protected static final String LAUNCH_URL_PATTERN = "LaunchURLPattern";
	protected static final String PARAM_TENANT_DOMAIN = "{tenantDomain}";
	protected static final String PARAM_APP_ID = "{applicationID}";
	protected static final String PARAM_APP_VERSION = "{applicationVersion}";
	protected static final String PARAM_APP_STAGE = "{stage}";
	protected static final String PARAM_APP_STAGE_NAME_SUFFIX = "StageParam";
	protected String name;
	protected String displayName;
	protected String descreption;
	protected String extension;
	protected String buildJobTemplate;
	protected Properties properties;

    public String getBuildJobTemplate() {
        return buildJobTemplate;
    }

    public void setBuildJobTemplate(String buildJobTemplate) {
        this.buildJobTemplate = buildJobTemplate;
    }

    public String getDescreption() {
        return descreption;
    }

    public void setDescreption(String descreption) {
        this.descreption = descreption;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getFileExtension() {
        return this.extension;
    }

    @Override
    public void setFileExtension(String name) {
        this.extension = name;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getProperty(String name) {
        return (String) this.properties.get(name);
    }

	@Override
	public List<File> getPreVersionDeleteableFiles(String appId, String targetVersion,
                                                   String currentVersion, String absolutePath) throws AppFactoryException {
	    return new ArrayList<File>();
    }

	public String getDeployedURL(String tenantDomain, String applicationID,
	                             String applicationVersion, String stage) throws AppFactoryException{
		String url = (String) this.properties.get(LAUNCH_URL_PATTERN);

		String artifactTrunkVersionName = AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty("TrunkVersioning.WebappVersioning.ArtifactVersionName");
		String sourceTrunkVersionName = AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty("TrunkVersioning.WebappVersioning.SourceVersionName");
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
}
