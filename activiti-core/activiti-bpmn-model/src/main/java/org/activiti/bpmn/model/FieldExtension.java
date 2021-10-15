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

public class FieldExtension extends BaseElement {

    protected String fieldName;
    protected String stringValue;
    protected String expression;

    public FieldExtension() {}

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public FieldExtension clone() {
        FieldExtension clone = new FieldExtension();
        clone.setValues(this);
        return clone;
    }

    public void setValues(FieldExtension otherExtension) {
        setFieldName(otherExtension.getFieldName());
        setStringValue(otherExtension.getStringValue());
        setExpression(otherExtension.getExpression());
    }
}
