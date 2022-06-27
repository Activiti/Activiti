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

public class VariableDefinition {

    private String id;

    private String name;

    private String description;

    private String type;

    private boolean required;

    private Boolean display;

    private String displayName;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public Boolean getDisplay() {
        return display;
    }

    public String getDisplayName() {
        return displayName;
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

    public void setType(String type) {
        this.type = type;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setDisplay(Boolean display) {
        this.display = display;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
