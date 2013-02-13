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

import java.util.Collection;

/**
 * @author Tijs Rademakers
 */
public interface FlowElementsContainer {

  FlowElement getFlowElement(String id);
  Collection<FlowElement> getFlowElements();
  void addFlowElement(FlowElement element);
  void removeFlowElement(String elementId);
  
  Artifact getArtifact(String id);
  Collection<Artifact> getArtifacts();
  void addArtifact(Artifact artifact);
  void removeArtifact(String artifactId);
}
