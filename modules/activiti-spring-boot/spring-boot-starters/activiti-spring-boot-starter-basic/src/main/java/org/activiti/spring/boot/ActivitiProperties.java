package org.activiti.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Josh Long
 * @author Joram Barrez
 */
@ConfigurationProperties("spring.activiti")
public class ActivitiProperties {

  private boolean checkProcessDefinitions = true;
  private boolean jobExecutorActivate = false;
  private boolean asyncExecutorEnabled = true;
  private boolean asyncExecutorActivate = true;
  private boolean restApiEnabled;
  private String deploymentName;
  private String mailServerHost = "localhost";
  private int mailServerPort = 1025;
  private String mailServerUserName;
  private String mailServerPassword;
  private String mailServerDefaultFrom;
  private boolean mailServerUseSsl;
  private boolean mailServerUseTls;
  private String databaseSchemaUpdate = "true";
  private String databaseSchema;
  private String processDefinitionLocationPrefix = "classpath:/processes/";
  private String processDefinitionLocationSuffix = "**.bpmn20.xml";
  private String restApiMapping = "/api/*";
  private String restApiServletName = "activitiRestApi";
  private boolean jpaEnabled = true; // true by default

  public boolean isJobExecutorActivate() {
    return jobExecutorActivate;
  }

  public void setJobExecutorActivate(boolean jobExecutorActivate) {
    this.jobExecutorActivate = jobExecutorActivate;
  }

  public boolean isAsyncExecutorEnabled() {
    return asyncExecutorEnabled;
  }

  public void setAsyncExecutorEnabled(boolean asyncExecutorEnabled) {
    this.asyncExecutorEnabled = asyncExecutorEnabled;
  }

  public boolean isAsyncExecutorActivate() {
    return asyncExecutorActivate;
  }

  public void setAsyncExecutorActivate(boolean asyncExecutorActivate) {
    this.asyncExecutorActivate = asyncExecutorActivate;
  }

  public boolean isRestApiEnabled() {
    return restApiEnabled;
  }

  public void setRestApiEnabled(boolean restApiEnabled) {
    this.restApiEnabled = restApiEnabled;
  }

  public boolean isJpaEnabled() {
    return jpaEnabled;
  }

  public void setJpaEnabled(boolean jpaEnabled) {
    this.jpaEnabled = jpaEnabled;
  }

  public String getRestApiMapping() {
    return restApiMapping;
  }

  public void setRestApiMapping(String restApiMapping) {
    this.restApiMapping = restApiMapping;
  }

  public String getRestApiServletName() {
    return restApiServletName;
  }

  public void setRestApiServletName(String restApiServletName) {
    this.restApiServletName = restApiServletName;
  }

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

  public String getMailServerHost() {
    return mailServerHost;
  }

  public void setMailServerHost(String mailServerHost) {
    this.mailServerHost = mailServerHost;
  }

  public int getMailServerPort() {
    return mailServerPort;
  }

  public void setMailServerPort(int mailServerPort) {
    this.mailServerPort = mailServerPort;
  }

	public String getMailServerUserName() {
		return mailServerUserName;
	}

	public void setMailServerUserName(String mailServerUserName) {
		this.mailServerUserName = mailServerUserName;
	}

	public String getMailServerPassword() {
		return mailServerPassword;
	}

	public void setMailServerPassword(String mailServerPassword) {
		this.mailServerPassword = mailServerPassword;
	}

	public String getMailServerDefaultFrom() {
		return mailServerDefaultFrom;
	}

	public void setMailServerDefaultFrom(String mailServerDefaultFrom) {
		this.mailServerDefaultFrom = mailServerDefaultFrom;
	}

	public boolean isMailServerUseSsl() {
		return mailServerUseSsl;
	}

	public void setMailServerUseSsl(boolean mailServerUseSsl) {
		this.mailServerUseSsl = mailServerUseSsl;
	}

	public boolean isMailServerUseTls() {
		return mailServerUseTls;
	}

	public void setMailServerUseTls(boolean mailServerUseTls) {
		this.mailServerUseTls = mailServerUseTls;
	}
	
}
