package org.wso2.carbon.appfactory.core.dto;

public class BuildandDeployStatus {

    private String lastBuildId;
    private String lastBuildStatus;
    private String lastDeployedId;

    public BuildandDeployStatus(String buildId, String buildStatus, String deployedId) {
        this.lastBuildId = buildId;
        this.lastBuildStatus = buildStatus;
        this.lastDeployedId = deployedId;
    }

    public String getLastBuildId() {
        return lastBuildId;
    }

    public void setLastBuildId(String lastBuildId) {
        this.lastBuildId = lastBuildId;
    }

    public String getLastBuildStatus() {
        return lastBuildStatus;
    }

    public void setLastBuildStatus(String lastBuildStatus) {
        this.lastBuildStatus = lastBuildStatus;
    }

    public String getLastDeployedId() {
        return lastDeployedId;
    }

    public void setLastDeployedId(String lastDeployedId) {
        this.lastDeployedId = lastDeployedId;
    }

}
