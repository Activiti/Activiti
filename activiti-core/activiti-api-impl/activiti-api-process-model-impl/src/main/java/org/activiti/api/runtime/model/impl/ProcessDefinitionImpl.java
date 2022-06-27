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
package org.activiti.api.runtime.model.impl;

import org.activiti.api.process.model.ProcessDefinition;

import java.util.Objects;

public class ProcessDefinitionImpl extends ApplicationElementImpl implements ProcessDefinition {

    private String id;
    private String name;
    private String description;
    private int version;
    private String key;
    private String formKey;
    private String category;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String getFormKey() {
        return formKey;
    }

    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }

    @Override
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ProcessDefinitionImpl that = (ProcessDefinitionImpl) o;
        return version == that.version &&
                Objects.equals(id,
                               that.id) &&
                Objects.equals(name,
                               that.name) &&
                Objects.equals(description,
                               that.description) &&
                Objects.equals(key,
                               that.key) &&
                Objects.equals(formKey,
                               that.formKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                            id,
                            name,
                            description,
                            version,
                            key,
                            formKey);
    }

    @Override
    public String toString() {
        return "ProcessDefinition{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", key='" + key + '\'' +
                ", description='" + description + '\'' +
                ", formKey='" + formKey + '\'' +
                ", version=" + version +
                '}';
    }
}
