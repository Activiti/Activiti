package org.activiti.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Josh Long
 * @author Joram Barrez
 */
@ConfigurationProperties("spring.activiti")
public class ActivitiProperties {

    private boolean checkProcessDefinitions;

    private String deploymentName;

    private String databaseSchemaUpdate  = "true" ;

    private String databaseSchema;

    private String processDefinitionLocationPrefix = "classpath:/processes/";

    private String processDefinitionLocationSuffix = "**.bpmn20.xml";

    private boolean jpa = true; // true by default

    public boolean isCheckProcessDefinitions() {
        return checkProcessDefinitions;
    }

    public void setCheckProcessDefinitions(boolean checkProcessDefinitions) {
        this.checkProcessDefinitions = checkProcessDefinitions;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public String getDatabaseSchemaUpdate() {
        return databaseSchemaUpdate;
    }

    public void setDatabaseSchemaUpdate(String databaseSchemaUpdate) {
        this.databaseSchemaUpdate = databaseSchemaUpdate;
    }

    public String getDatabaseSchema() {
        return databaseSchema;
    }

    public void setDatabaseSchema(String databaseSchema) {
        this.databaseSchema = databaseSchema;
    }

    public String getProcessDefinitionLocationPrefix() {
        return processDefinitionLocationPrefix;
    }

    public void setProcessDefinitionLocationPrefix(
            String processDefinitionLocationPrefix) {
        this.processDefinitionLocationPrefix = processDefinitionLocationPrefix;
    }

    public String getProcessDefinitionLocationSuffix() {
        return processDefinitionLocationSuffix;
    }

    public void setProcessDefinitionLocationSuffix(
            String processDefinitionLocationSuffix) {
        this.processDefinitionLocationSuffix = processDefinitionLocationSuffix;
    }

    public boolean isJpa() {
        return jpa;
    }

    public void setJpa(boolean jpa) {
        this.jpa = jpa;
    }
}
