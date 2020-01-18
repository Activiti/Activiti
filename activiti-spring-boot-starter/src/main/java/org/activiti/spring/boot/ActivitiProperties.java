/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.spring.boot;

import java.util.Arrays;
import java.util.List;

import org.activiti.engine.impl.history.HistoryLevel;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties("spring.activiti")
public class ActivitiProperties {

  private boolean checkProcessDefinitions = true;
  private boolean asyncExecutorActivate = false;
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
  private boolean isDbHistoryUsed = false;
  private HistoryLevel historyLevel = HistoryLevel.NONE;
  private String processDefinitionLocationPrefix = "classpath:/processes/";
  private List<String> processDefinitionLocationSuffixes = Arrays.asList("**.bpmn20.xml", "**.bpmn");
  private List<String> customMybatisMappers;
  private List<String> customMybatisXMLMappers;
  private boolean useStrongUuids = true;

  public boolean isAsyncExecutorActivate() {
    return asyncExecutorActivate;
  }

  public void setAsyncExecutorActivate(boolean asyncExecutorActivate) {
    this.asyncExecutorActivate = asyncExecutorActivate;
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

  public boolean isDbHistoryUsed() {
    return isDbHistoryUsed;
  }

  public void setDbHistoryUsed(boolean isDbHistoryUsed) {
    this.isDbHistoryUsed = isDbHistoryUsed;
  }

  public HistoryLevel getHistoryLevel() {
    return historyLevel;
  }

  public void setHistoryLevel(HistoryLevel historyLevel) {
    this.historyLevel = historyLevel;
  }

  public String getProcessDefinitionLocationPrefix() {
    return processDefinitionLocationPrefix;
  }

  public void setProcessDefinitionLocationPrefix(
      String processDefinitionLocationPrefix) {
    this.processDefinitionLocationPrefix = processDefinitionLocationPrefix;
  }

  public List<String> getProcessDefinitionLocationSuffixes() {
		return processDefinitionLocationSuffixes;
	}

	public void setProcessDefinitionLocationSuffixes(
	    List<String> processDefinitionLocationSuffixes) {
		this.processDefinitionLocationSuffixes = processDefinitionLocationSuffixes;
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

  public List<String> getCustomMybatisMappers() {
    return customMybatisMappers;
  }

  public void setCustomMybatisMappers(List<String> customMyBatisMappers) {
    this.customMybatisMappers = customMyBatisMappers;
  }

  public List<String> getCustomMybatisXMLMappers() {
    return customMybatisXMLMappers;
  }

  public void setCustomMybatisXMLMappers(List<String> customMybatisXMLMappers) {
    this.customMybatisXMLMappers = customMybatisXMLMappers;
  }
  
  public boolean isUseStrongUuids() {
	return useStrongUuids;
  }
  
  public void setUseStrongUuids(boolean useStrongUuids) {
	this.useStrongUuids = useStrongUuids;
  }
  
}
