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

public class Pool extends BaseElement {

  protected String name;
  protected String processRef;
  protected boolean executable = true;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getProcessRef() {
    return processRef;
  }

  public void setProcessRef(String processRef) {
    this.processRef = processRef;
  }

  public boolean isExecutable() {
    return this.executable;
  }

  public void setExecutable(boolean executable) {
    this.executable = executable;
  }

  public Pool clone() {
    Pool clone = new Pool();
    clone.setValues(this);
    return clone;
  }

  public void setValues(Pool otherElement) {
    super.setValues(otherElement);
    setName(otherElement.getName());
    setProcessRef(otherElement.getProcessRef());
    setExecutable(otherElement.isExecutable());
  }
}
