/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

public class LinkEventDefinition extends EventDefinition {

    private String name;

    private String target;

    private List<String> sources;

    public LinkEventDefinition clone() {
        LinkEventDefinition clone = new LinkEventDefinition();
        clone.setValues(this);
        return clone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public void addSource(String source) {
        if (sources == null) {
            sources = new ArrayList<>();
        }
        sources.add(source);
    }

    public void setValues(LinkEventDefinition otherDefinition) {
        super.setValues(otherDefinition);
        setName(otherDefinition.getName());
        setTarget(otherDefinition.getTarget());
        setSources(otherDefinition.getSources());
    }
}
