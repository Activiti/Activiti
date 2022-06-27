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


package org.activiti.spring.process.model;

import java.util.HashMap;
import java.util.Map;

public class ProcessVariablesMapping {

    private MappingType mappingType;
    private Map<String, Mapping> inputs = new HashMap<>();
    private Map<String, Mapping> outputs = new HashMap<>();

    public Map<String, Mapping> getInputs() {
        return inputs;
    }
    public void setInputs(Map<String, Mapping> inputs) {
        this.inputs = inputs;
    }

    public Mapping getInputMapping(String inputName) {
        return inputs.get(inputName);
    }

    public Map<String, Mapping> getOutputs() {
        return outputs;
    }
    public void setOutputs(Map<String, Mapping> outputs) {
        this.outputs = outputs;
    }

    public MappingType getMappingType() {
        return mappingType;
    }

    public void setMappingType(MappingType mappingType) {
        this.mappingType = mappingType;
    }

    public enum MappingType {
        MAP_ALL,
        MAP_ALL_INPUTS,
        MAP_ALL_OUTPUTS
    }
}
