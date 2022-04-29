/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

public class IOSpecification extends BaseElement {

  protected List<DataSpec> dataInputs = new ArrayList<DataSpec>();
  protected List<DataSpec> dataOutputs = new ArrayList<DataSpec>();
  protected List<String> dataInputRefs = new ArrayList<String>();
  protected List<String> dataOutputRefs = new ArrayList<String>();

  public List<DataSpec> getDataInputs() {
    return dataInputs;
  }

  public void setDataInputs(List<DataSpec> dataInputs) {
    this.dataInputs = dataInputs;
  }

  public List<DataSpec> getDataOutputs() {
    return dataOutputs;
  }

  public void setDataOutputs(List<DataSpec> dataOutputs) {
    this.dataOutputs = dataOutputs;
  }

  public List<String> getDataInputRefs() {
    return dataInputRefs;
  }

  public void setDataInputRefs(List<String> dataInputRefs) {
    this.dataInputRefs = dataInputRefs;
  }

  public List<String> getDataOutputRefs() {
    return dataOutputRefs;
  }

  public void setDataOutputRefs(List<String> dataOutputRefs) {
    this.dataOutputRefs = dataOutputRefs;
  }

  public IOSpecification clone() {
    IOSpecification clone = new IOSpecification();
    clone.setValues(this);
    return clone;
  }

  public void setValues(IOSpecification otherSpec) {
    dataInputs = new ArrayList<DataSpec>();
    if (otherSpec.getDataInputs() != null && !otherSpec.getDataInputs().isEmpty()) {
      for (DataSpec dataSpec : otherSpec.getDataInputs()) {
        dataInputs.add(dataSpec.clone());
      }
    }

    dataOutputs = new ArrayList<DataSpec>();
    if (otherSpec.getDataOutputs() != null && !otherSpec.getDataOutputs().isEmpty()) {
      for (DataSpec dataSpec : otherSpec.getDataOutputs()) {
        dataOutputs.add(dataSpec.clone());
      }
    }

    dataInputRefs = new ArrayList<String>(otherSpec.getDataInputRefs());
    dataOutputRefs = new ArrayList<String>(otherSpec.getDataOutputRefs());
  }
}
