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

public abstract class ValuedDataObject extends DataObject {

    protected Object value;

    public Object getValue() {
        return value;
    }

    public abstract void setValue(Object value);

    public abstract ValuedDataObject clone();

    public void setValues(ValuedDataObject otherElement) {
        super.setValues(otherElement);
        if (otherElement.getValue() != null) {
            setValue(otherElement.getValue());
        }
    }

    public String getType() {
        String structureRef = itemSubjectRef.getStructureRef();
        return structureRef.substring(structureRef.indexOf(':') + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ValuedDataObject otherObject = (ValuedDataObject) o;

        if (!otherObject.getItemSubjectRef().getStructureRef().equals(this.itemSubjectRef.getStructureRef())) {
            return false;
        }
        if (!otherObject.getId().equals(this.id)) {
            return false;
        }
        if (!otherObject.getName().equals(this.name)) {
            return false;
        }
        if (!otherObject.getValue().equals(this.value.toString())) {
            return false;
        }

        return true;
    }
}
