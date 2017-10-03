/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.services.connectors.model;

import java.util.HashMap;
import java.util.Map;

public class ServiceTaskEvent {

    private String id;
    private AsyncContext context;

    private Map<String, Object> variables;

    //used by json deserialization
    public ServiceTaskEvent() {
    }

    public ServiceTaskEvent(String id,
                            AsyncContext context,
                            Map<String, Object> variables) {
        this.id = id;
        this.context = context;
        this.variables = variables;
    }

    public String getId() {
        return id;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void putVariable(String name, Object value) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.put(name, value);
    }

    public AsyncContext getContext() {
        return context;
    }

}
