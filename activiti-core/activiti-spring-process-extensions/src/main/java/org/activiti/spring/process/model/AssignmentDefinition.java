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
package org.activiti.spring.process.model;

import java.util.Objects;

public class AssignmentDefinition {

    public enum AssignmentEnum {
        ASSIGNEE("assignee"),
        CANDIDATES("candidates");

        private String assignment;

        private AssignmentEnum(String assignment) {
            this.assignment = assignment;
        }
    }

    public enum AssignmentType {
        STATIC("static"),
        IDENTITY("identity"),
        EXPRESSION("expression");

        private String type;

        private AssignmentType(String type) {
            this.type = type;
        }

    }

    public enum AssignmentMode {
        SEQUENTIAL("sequential"),
        MANUAL("manual");

        private String mode;

        private AssignmentMode(String mode) {
            this.mode = mode;
        }

    }

    private String id;

    private AssignmentEnum assignment;
    private AssignmentType type;
    private AssignmentMode mode;

    AssignmentDefinition() {
    }

    public AssignmentDefinition(String id,
                                AssignmentEnum assignment,
                                AssignmentType type,
                                AssignmentMode mode) {
        this.id = id;
        this.assignment = assignment;
        this.type = type;
        this.mode = mode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AssignmentEnum getAssignment() {
        return assignment;
    }

    public void setAssignment(AssignmentEnum assignment) {
        this.assignment = assignment;
    }

    public AssignmentType getType() {
        return type;
    }

    public void setType(AssignmentType type) {
        this.type = type;
    }

    public AssignmentMode getMode() {
        return mode;
    }

    public void setMode(AssignmentMode mode) {
        this.mode = mode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignmentDefinition that = (AssignmentDefinition) o;
        return Objects.equals(id,
                              that.id) && assignment == that.assignment && type == that.type && mode == that.mode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,
                            assignment,
                            type,
                            mode);
    }

    @Override
    public String toString() {
        return "AssignmentDefinition{" +
            "id='" + id + '\'' +
            ", assignment=" + assignment +
            ", type=" + type +
            ", mode=" + mode +
            '}';
    }
}
