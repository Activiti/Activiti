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

public class ProcessExtensionModel {

    private String id;
    private Map<String, Extension> extensions = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Extension getExtensions(String processDefinitionKey) {
        return extensions.get(processDefinitionKey);
    }

    public Map<String, Extension> getAllExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, Extension> extensions) {
        this.extensions = extensions;
    }
}
