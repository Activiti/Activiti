/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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

import static org.activiti.spring.process.model.AssignmentDefinition.AssignmentEnum.ASSIGNEE;
import static org.activiti.spring.process.model.AssignmentDefinition.AssignmentMode.MANUAL;
import static org.activiti.spring.process.model.AssignmentDefinition.AssignmentType.STATIC;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AssignmentDefinitionTest {

    @Test
    void should_defaultAllowSelfServiceToFalse_when_notExplicitlySet() {
        AssignmentDefinition assignment = new AssignmentDefinition("1", ASSIGNEE, STATIC, MANUAL);

        assertThat(assignment.isAllowSelfService()).isFalse();
    }

    @Test
    void should_setAndGetAllowSelfService() {
        AssignmentDefinition assignment = new AssignmentDefinition("1", ASSIGNEE, STATIC, MANUAL);

        assignment.setAllowSelfService(true);

        assertThat(assignment.isAllowSelfService()).isTrue();
    }

    @Test
    void should_beEqual_when_allFieldsIncludingAllowSelfServiceMatch() {
        AssignmentDefinition assignment1 = new AssignmentDefinition("1", ASSIGNEE, STATIC, MANUAL);
        assignment1.setAllowSelfService(true);

        AssignmentDefinition assignment2 = new AssignmentDefinition("1", ASSIGNEE, STATIC, MANUAL);
        assignment2.setAllowSelfService(true);

        assertThat(assignment1).isEqualTo(assignment2).hasSameHashCodeAs(assignment2);
    }

    @Test
    void should_beEqual_when_sameInstance() {
        AssignmentDefinition assignment = new AssignmentDefinition("1", ASSIGNEE, STATIC, MANUAL);

        assertThat(assignment).isEqualTo(assignment);
    }

    @Test
    void should_notBeEqual_when_allowSelfServiceDiffers() {
        AssignmentDefinition assignment1 = new AssignmentDefinition("1", ASSIGNEE, STATIC, MANUAL);
        assignment1.setAllowSelfService(true);

        AssignmentDefinition assignment2 = new AssignmentDefinition("1", ASSIGNEE, STATIC, MANUAL);
        assignment2.setAllowSelfService(false);

        assertThat(assignment1).isNotEqualTo(assignment2);
    }

    @Test
    void should_notBeEqual_when_comparedToNull() {
        AssignmentDefinition assignment = new AssignmentDefinition("1", ASSIGNEE, STATIC, MANUAL);

        assertThat(assignment).isNotEqualTo(null);
    }

    @Test
    void should_notBeEqual_when_comparedToDifferentClass() {
        AssignmentDefinition assignment = new AssignmentDefinition("1", ASSIGNEE, STATIC, MANUAL);

        assertThat(assignment).isNotEqualTo("not an AssignmentDefinition");
    }

    @Test
    void should_includeAllowSelfService_when_toStringIsCalled() {
        AssignmentDefinition assignment = new AssignmentDefinition("1", ASSIGNEE, STATIC, MANUAL);
        assignment.setAllowSelfService(true);

        assertThat(assignment.toString()).contains("allowSelfService=true");
    }
}
