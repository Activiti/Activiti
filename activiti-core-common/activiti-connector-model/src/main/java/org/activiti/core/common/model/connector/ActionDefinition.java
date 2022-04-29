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
package org.activiti.core.common.model.connector;

import static java.util.Collections.emptyList;

import java.util.List;

public class ActionDefinition {

    private String id;

    private String name;

    private String description;

    private List<VariableDefinition> inputs;

    private List<VariableDefinition> outputs;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<VariableDefinition> getInputs() {
        return inputs != null ? inputs : emptyList();
    }

    public List<VariableDefinition> getOutputs() {
        return outputs != null ? outputs : emptyList();
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setInputs(List<VariableDefinition> inputs) {
        this.inputs = inputs;
    }

    public void setOutputs(List<VariableDefinition> outputs) {
        this.outputs = outputs;
    }
}
