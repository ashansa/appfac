package org.wso2.carbon.appfactory.stratos.services;

public class AppFactoryResource {
	private String resourceContent;
	private String description;
	private String mediaType;
	private boolean isCollection = false;
	private String resourcePath;
	
	public AppFactoryResource(){
		
	}

	public boolean isCollection() {
		return this.isCollection;
	}

	public void setCollection(boolean isCollection) {
		this.isCollection = isCollection;
	}

	private ResourceProperty[] resourceProperties = null;

	private AppFactoryResource[] appFactoryResources = null;

	public AppFactoryResource(String resourcePath, String resourceContent) {
		this.resourcePath = resourcePath;
		this.resourceContent = resourceContent;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public String getResourceContent() {
		return this.resourceContent;
	}

	public void setResourceContent(String resourceContent) {
		this.resourceContent = resourceContent;
	}

	public ResourceProperty[] getResourceProperties() {
		return this.resourceProperties;
	}

	public void setResourceProperties(ResourceProperty[] resourceProperties) {
		this.resourceProperties = resourceProperties;
	}

	public AppFactoryResource[] getAppFactoryResources() {
		return this.appFactoryResources;
	}

	public void setAppFactoryResources(AppFactoryResource[] appFactoryResources) {
		this.appFactoryResources = appFactoryResources;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMediaType() {
		return this.mediaType;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}
}