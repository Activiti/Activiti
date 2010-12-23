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
package org.activiti.kickstart.dto;

import java.util.Date;

/**
 * @author Joram Barrez
 */
public class AdhocWorkflowInfo {

  protected String id;
  protected String key;
  protected String name;
  protected int version;
  protected Date createTime;
  protected long nrOfRuntimeInstances;
  protected long nrOfHistoricInstances;
  protected String deploymentId;

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getKey() {
    return key;
  }
  public void setKey(String key) {
    this.key = key;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public int getVersion() {
    return version;
  }
  public void setVersion(int version) {
    this.version = version;
  }
  public Date getCreateTime() {
    return createTime;
  }
  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }
  public long getNrOfRuntimeInstances() {
    return nrOfRuntimeInstances;
  }
  public void setNrOfRuntimeInstances(long nrOfRuntimeInstances) {
    this.nrOfRuntimeInstances = nrOfRuntimeInstances;
  }
  public long getNrOfHistoricInstances() {
    return nrOfHistoricInstances;
  }
  public void setNrOfHistoricInstances(long nrOfHistoricInstances) {
    this.nrOfHistoricInstances = nrOfHistoricInstances;
  }
  public String getDeploymentId() {
    return deploymentId;
  }
  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

}
