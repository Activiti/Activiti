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
import java.util.List;

/**
 * @author Tijs Rademakers
 */
public class CallActivity extends Activity {

  protected String calledElement;
  protected boolean inheritVariables;
  protected List<IOParameter> inParameters = new ArrayList<IOParameter>();
  protected List<IOParameter> outParameters = new ArrayList<IOParameter>();

  public String getCalledElement() {
    return calledElement;
  }
  public void setCalledElement(String calledElement) {
    this.calledElement = calledElement;
  }
  public boolean isInheritVariables() {
    return inheritVariables;
  }
  public void setInheritVariables(boolean inheritVariables) {
    this.inheritVariables = inheritVariables;
  }
  public List<IOParameter> getInParameters() {
    return inParameters;
  }
  public void setInParameters(List<IOParameter> inParameters) {
    this.inParameters = inParameters;
  }
  public List<IOParameter> getOutParameters() {
    return outParameters;
  }
  public void setOutParameters(List<IOParameter> outParameters) {
    this.outParameters = outParameters;
  }
  
  public CallActivity clone() {
    CallActivity clone = new CallActivity();
    clone.setValues(this);
    return clone;
  }
  
  public void setValues(CallActivity otherElement) {
    super.setValues(otherElement);
    setCalledElement(otherElement.getCalledElement());
    
    inParameters = new ArrayList<IOParameter>();
    if (otherElement.getInParameters() != null && !otherElement.getInParameters().isEmpty()) {
      for (IOParameter parameter : otherElement.getInParameters()) {
        inParameters.add(parameter.clone());
      }
    }
    
    outParameters = new ArrayList<IOParameter>();
    if (otherElement.getOutParameters() != null && !otherElement.getOutParameters().isEmpty()) {
      for (IOParameter parameter : otherElement.getOutParameters()) {
        outParameters.add(parameter.clone());
      }
    }
  }
}
