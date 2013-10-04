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

import org.codehaus.jackson.annotate.JsonBackReference;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tijs Rademakers
 */
public class Lane extends BaseElement {
  
  protected String name;
  protected Process parentProcess;
  protected List<String> flowReferences = new ArrayList<String>();
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @JsonBackReference
  public Process getParentProcess() {
    return parentProcess;
  }
  
  public void setParentProcess(Process parentProcess) {
    this.parentProcess = parentProcess;
  }

  public List<String> getFlowReferences() {
    return flowReferences;
  }

  public void setFlowReferences(List<String> flowReferences) {
    this.flowReferences = flowReferences;
  }
}
