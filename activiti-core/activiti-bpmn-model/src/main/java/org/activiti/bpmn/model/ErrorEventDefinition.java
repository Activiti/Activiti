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

public class ErrorEventDefinition extends EventDefinition {

    protected String errorRef;

    public String getErrorRef() {
        return errorRef;
    }

    public void setErrorRef(String errorRef) {
        this.errorRef = errorRef;
    }

    public ErrorEventDefinition clone() {
        ErrorEventDefinition clone = new ErrorEventDefinition();
        clone.setValues(this);
        return clone;
    }

    public void setValues(ErrorEventDefinition otherDefinition) {
        super.setValues(otherDefinition);
        setErrorRef(otherDefinition.getErrorRef());
    }
}
