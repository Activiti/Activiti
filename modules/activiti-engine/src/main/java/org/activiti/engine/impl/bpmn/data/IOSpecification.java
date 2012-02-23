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
package org.activiti.engine.impl.bpmn.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/**
 * Implementation of the BPMN 2.0 'ioSpecification'
 * 
 * @author Esteban Robles Luna
 * @author Falko Menge
 */
public class IOSpecification {
  
  protected List<Data> dataInputs;
  
  protected List<Data> dataOutputs;
  
  protected List<DataRef> dataInputRefs;

  protected List<DataRef> dataOutputRefs;

  public IOSpecification() {
    this.dataInputs = new ArrayList<Data>();
    this.dataOutputs = new ArrayList<Data>();
    this.dataInputRefs = new ArrayList<DataRef>();
    this.dataOutputRefs = new ArrayList<DataRef>();
  }
  
  public void initialize(ActivityExecution execution) {
    for (Data data : this.dataInputs) {
      execution.setVariable(data.getName(), data.getDefinition().createInstance());
    }

    for (Data data : this.dataOutputs) {
      execution.setVariable(data.getName(), data.getDefinition().createInstance());
    }
  }
  
  public List<Data> getDataInputs() {
    return Collections.unmodifiableList(this.dataInputs);
  }

  public List<Data> getDataOutputs() {
    return Collections.unmodifiableList(this.dataOutputs);
  }

  public void addInput(Data data) {
    this.dataInputs.add(data);
  }

  public void addOutput(Data data) {
    this.dataOutputs.add(data);
  }

  public void addInputRef(DataRef dataRef) {
    this.dataInputRefs.add(dataRef);
  }

  public void addOutputRef(DataRef dataRef) {
    this.dataOutputRefs.add(dataRef);
  }

  public String getFirstDataInputName() {
    return this.dataInputs.get(0).getName();
  }

  public String getFirstDataOutputName() {
    if (this.dataOutputs != null && !this.dataOutputs.isEmpty()) {
      return this.dataOutputs.get(0).getName();
    } else {
      return null;
    }
  }
}
