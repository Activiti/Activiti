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
package org.activiti.bpmn.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tijs Rademakers
 */
public class Process extends FlowElementsContainer {

  protected String name;
  protected boolean executable;
  protected String documentation;
  protected List<ActivitiListener> executionListeners = new ArrayList<ActivitiListener>();
  protected List<Lane> lanes = new ArrayList<Lane>();
  protected Map<String, Artifact> artifactMap = new LinkedHashMap<String, Artifact>();
  protected List<String> candidateStarterUsers = new ArrayList<String>();
  protected List<String> candidateStarterGroups = new ArrayList<String>();

  public String getDocumentation() {
    return documentation;
  }

  public void setDocumentation(String documentation) {
    this.documentation = documentation;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isExecutable() {
    return executable;
  }

  public void setExecutable(boolean executable) {
    this.executable = executable;
  }

  public List<ActivitiListener> getExecutionListeners() {
    return executionListeners;
  }

  public void setExecutionListeners(List<ActivitiListener> executionListeners) {
    this.executionListeners = executionListeners;
  }

  public List<Lane> getLanes() {
    return lanes;
  }

  public void setLanes(List<Lane> lanes) {
    this.lanes = lanes;
  }

  public Artifact getArtifact(String id) {
    return artifactMap.get(id);
  }
  
  public Collection<Artifact> getArtifacts() {
    return artifactMap.values();
  }
  
  public Map<String, Artifact> getArtifactMap() {
    return artifactMap;
  }
  
  public void addArtifact(Artifact artifact) {
    artifactMap.put(artifact.getId(), artifact);
  }
  
  public void removeArtifact(String artifactId) {
    artifactMap.remove(artifactId);
  }

  public List<String> getCandidateStarterUsers() {
    return candidateStarterUsers;
  }

  public void setCandidateStarterUsers(List<String> candidateStarterUsers) {
    this.candidateStarterUsers = candidateStarterUsers;
  }

  public List<String> getCandidateStarterGroups() {
    return candidateStarterGroups;
  }

  public void setCandidateGroups(List<String> candidateStarterGroups) {
    this.candidateStarterGroups = candidateStarterGroups;
  }
}
