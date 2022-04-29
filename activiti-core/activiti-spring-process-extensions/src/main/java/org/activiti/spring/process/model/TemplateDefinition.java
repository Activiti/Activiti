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

import java.util.Objects;

public class TemplateDefinition {

    public enum TemplateType {
        VARIABLE,
        FILE;
    }

    private String from;

    private String subject;

    private TemplateType type;

    private String value;

    TemplateDefinition() {
    }

    public TemplateDefinition(TemplateType type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TemplateType getType() {
        return type;
    }

    public void setType(TemplateType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateDefinition that = (TemplateDefinition) o;
        return Objects.equals(from, that.from) &&
            Objects.equals(subject, that.subject) &&
            type == that.type &&
            Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from,
                            subject,
                            type,
                            value);
    }

    @Override
    public String toString() {
        return "TemplateDefinition{" +
            "from='" + from + '\'' +
            ", subject='" + subject + '\'' +
            ", type=" + type +
            ", value='" + value + '\'' +
            '}';
    }
}
