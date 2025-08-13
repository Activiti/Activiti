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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskTemplateDefinitionTest {

    @Test
    void should_createTaskTemplateDefinition_withDefaultValues() {
        TaskTemplateDefinition taskTemplate = new TaskTemplateDefinition();

        assertThat(taskTemplate.getAssignee()).isNull();
        assertThat(taskTemplate.getCandidate()).isNull();
        assertThat(taskTemplate.isEmailNotificationEnabled()).isTrue();
    }

    @Test
    void should_setAndGetAssignee() {
        TaskTemplateDefinition taskTemplate = new TaskTemplateDefinition();
        TemplateDefinition assigneeTemplate = new TemplateDefinition(TemplateDefinition.TemplateType.VARIABLE, "assigneeTemplate");

        taskTemplate.setAssignee(assigneeTemplate);

        assertThat(taskTemplate.getAssignee()).isEqualTo(assigneeTemplate);
        assertThat(taskTemplate.getAssignee().getType()).isEqualTo(TemplateDefinition.TemplateType.VARIABLE);
        assertThat(taskTemplate.getAssignee().getValue()).isEqualTo("assigneeTemplate");
    }

    @Test
    void should_setAndGetAssignee_withNullValue() {
        TaskTemplateDefinition taskTemplate = new TaskTemplateDefinition();
        TemplateDefinition assigneeTemplate = new TemplateDefinition(TemplateDefinition.TemplateType.VARIABLE, "test");

        taskTemplate.setAssignee(assigneeTemplate);
        assertThat(taskTemplate.getAssignee()).isNotNull();

        taskTemplate.setAssignee(null);
        assertThat(taskTemplate.getAssignee()).isNull();
    }

    @Test
    void should_setAndGetCandidate() {
        TaskTemplateDefinition taskTemplate = new TaskTemplateDefinition();
        TemplateDefinition candidateTemplate = new TemplateDefinition(TemplateDefinition.TemplateType.FILE, "candidateTemplate");

        taskTemplate.setCandidate(candidateTemplate);

        assertThat(taskTemplate.getCandidate()).isEqualTo(candidateTemplate);
        assertThat(taskTemplate.getCandidate().getType()).isEqualTo(TemplateDefinition.TemplateType.FILE);
        assertThat(taskTemplate.getCandidate().getValue()).isEqualTo("candidateTemplate");
    }

    @Test
    void should_setAndGetCandidate_withNullValue() {
        TaskTemplateDefinition taskTemplate = new TaskTemplateDefinition();
        TemplateDefinition candidateTemplate = new TemplateDefinition(TemplateDefinition.TemplateType.FILE, "test");

        taskTemplate.setCandidate(candidateTemplate);
        assertThat(taskTemplate.getCandidate()).isNotNull();

        taskTemplate.setCandidate(null);
        assertThat(taskTemplate.getCandidate()).isNull();
    }

    @Test
    void should_setAndGetEmailNotificationEnabled_true() {
        TaskTemplateDefinition taskTemplate = new TaskTemplateDefinition();

        taskTemplate.setEmailNotificationEnabled(true);

        assertThat(taskTemplate.isEmailNotificationEnabled()).isTrue();
    }

    @Test
    void should_setAndGetEmailNotificationEnabled_false() {
        TaskTemplateDefinition taskTemplate = new TaskTemplateDefinition();

        taskTemplate.setEmailNotificationEnabled(false);

        assertThat(taskTemplate.isEmailNotificationEnabled()).isFalse();
    }

    @Test
    void should_toggleEmailNotificationEnabled() {
        TaskTemplateDefinition taskTemplate = new TaskTemplateDefinition();

        assertThat(taskTemplate.isEmailNotificationEnabled()).isTrue();

        taskTemplate.setEmailNotificationEnabled(false);
        assertThat(taskTemplate.isEmailNotificationEnabled()).isFalse();

        taskTemplate.setEmailNotificationEnabled(true);
        assertThat(taskTemplate.isEmailNotificationEnabled()).isTrue();
    }

    @Test
    void should_testEquals_withSameObject() {
        TaskTemplateDefinition taskTemplate = new TaskTemplateDefinition();

        assertThat(taskTemplate).isEqualTo(taskTemplate);
    }

    @Test
    void should_testEquals_withNullObject() {
        TaskTemplateDefinition taskTemplate = new TaskTemplateDefinition();

        assertThat(taskTemplate).isNotEqualTo(null);
    }

    @Test
    void should_testEquals_withEqualObjects() {
        TaskTemplateDefinition taskTemplate1 = new TaskTemplateDefinition();
        TaskTemplateDefinition taskTemplate2 = new TaskTemplateDefinition();

        TemplateDefinition assigneeTemplate = new TemplateDefinition(TemplateDefinition.TemplateType.VARIABLE, "assignee");
        TemplateDefinition candidateTemplate = new TemplateDefinition(TemplateDefinition.TemplateType.FILE, "candidate");

        taskTemplate1.setAssignee(assigneeTemplate);
        taskTemplate1.setCandidate(candidateTemplate);
        taskTemplate1.setEmailNotificationEnabled(false);

        taskTemplate2.setAssignee(assigneeTemplate);
        taskTemplate2.setCandidate(candidateTemplate);
        taskTemplate2.setEmailNotificationEnabled(false);

        assertThat(taskTemplate1).isEqualTo(taskTemplate2);
        assertThat(taskTemplate2).isEqualTo(taskTemplate1);
        assertThat(taskTemplate1).hasSameHashCodeAs(taskTemplate2);
    }

    @Test
    void should_testEquals_withDifferentAssignee() {
        TaskTemplateDefinition taskTemplate1 = new TaskTemplateDefinition();
        TaskTemplateDefinition taskTemplate2 = new TaskTemplateDefinition();

        TemplateDefinition assigneeTemplate1 = new TemplateDefinition(TemplateDefinition.TemplateType.VARIABLE, "assignee1");
        TemplateDefinition assigneeTemplate2 = new TemplateDefinition(TemplateDefinition.TemplateType.VARIABLE, "assignee2");

        taskTemplate1.setAssignee(assigneeTemplate1);
        taskTemplate2.setAssignee(assigneeTemplate2);

        assertThat(taskTemplate1).isNotEqualTo(taskTemplate2);
    }

    @Test
    void should_testEquals_withOneNullAssignee() {
        TaskTemplateDefinition taskTemplate1 = new TaskTemplateDefinition();
        TaskTemplateDefinition taskTemplate2 = new TaskTemplateDefinition();

        TemplateDefinition assigneeTemplate = new TemplateDefinition(TemplateDefinition.TemplateType.VARIABLE, "assignee");

        taskTemplate1.setAssignee(assigneeTemplate);
        taskTemplate2.setAssignee(null);

        assertThat(taskTemplate1).isNotEqualTo(taskTemplate2);
    }

    @Test
    void should_testEquals_withBothNullAssignees() {
        TaskTemplateDefinition taskTemplate1 = new TaskTemplateDefinition();
        TaskTemplateDefinition taskTemplate2 = new TaskTemplateDefinition();

        taskTemplate1.setAssignee(null);
        taskTemplate2.setAssignee(null);

        assertThat(taskTemplate1).isEqualTo(taskTemplate2);
    }

    @Test
    void should_testEquals_withDifferentCandidate() {
        TaskTemplateDefinition taskTemplate1 = new TaskTemplateDefinition();
        TaskTemplateDefinition taskTemplate2 = new TaskTemplateDefinition();

        TemplateDefinition candidateTemplate1 = new TemplateDefinition(TemplateDefinition.TemplateType.VARIABLE, "candidate1");
        TemplateDefinition candidateTemplate2 = new TemplateDefinition(TemplateDefinition.TemplateType.VARIABLE, "candidate2");

        taskTemplate1.setCandidate(candidateTemplate1);
        taskTemplate2.setCandidate(candidateTemplate2);

        assertThat(taskTemplate1).isNotEqualTo(taskTemplate2);
    }

    @Test
    void should_testEquals_withOneNullCandidate() {
        TaskTemplateDefinition taskTemplate1 = new TaskTemplateDefinition();
        TaskTemplateDefinition taskTemplate2 = new TaskTemplateDefinition();

        TemplateDefinition candidateTemplate = new TemplateDefinition(TemplateDefinition.TemplateType.FILE, "candidate");

        taskTemplate1.setCandidate(candidateTemplate);
        taskTemplate2.setCandidate(null);

        assertThat(taskTemplate1).isNotEqualTo(taskTemplate2);
    }

    @Test
    void should_testEquals_withBothNullCandidates() {
        TaskTemplateDefinition taskTemplate1 = new TaskTemplateDefinition();
        TaskTemplateDefinition taskTemplate2 = new TaskTemplateDefinition();

        taskTemplate1.setCandidate(null);
        taskTemplate2.setCandidate(null);

        assertThat(taskTemplate1).isEqualTo(taskTemplate2);
    }

    @Test
    void should_testEquals_withDifferentEmailNotificationEnabled() {
        TaskTemplateDefinition taskTemplate1 = new TaskTemplateDefinition();
        TaskTemplateDefinition taskTemplate2 = new TaskTemplateDefinition();

        taskTemplate1.setEmailNotificationEnabled(true);
        taskTemplate2.setEmailNotificationEnabled(false);

        assertThat(taskTemplate1).isNotEqualTo(taskTemplate2);
    }

    @Test
    void should_testEquals_transitiveProperty() {
        TaskTemplateDefinition taskTemplate1 = new TaskTemplateDefinition();
        TaskTemplateDefinition taskTemplate2 = new TaskTemplateDefinition();
        TaskTemplateDefinition taskTemplate3 = new TaskTemplateDefinition();

        TemplateDefinition assigneeTemplate = new TemplateDefinition(TemplateDefinition.TemplateType.VARIABLE, "assignee");

        taskTemplate1.setAssignee(assigneeTemplate);
        taskTemplate1.setEmailNotificationEnabled(false);

        taskTemplate2.setAssignee(assigneeTemplate);
        taskTemplate2.setEmailNotificationEnabled(false);

        taskTemplate3.setAssignee(assigneeTemplate);
        taskTemplate3.setEmailNotificationEnabled(false);

        assertThat(taskTemplate1).isEqualTo(taskTemplate2);
        assertThat(taskTemplate2).isEqualTo(taskTemplate3);
        assertThat(taskTemplate1).isEqualTo(taskTemplate3);
    }

    @Test
    void should_testHashCode_consistency() {
        TaskTemplateDefinition taskTemplate = new TaskTemplateDefinition();
        TemplateDefinition assigneeTemplate = new TemplateDefinition(TemplateDefinition.TemplateType.VARIABLE, "assignee");

        taskTemplate.setAssignee(assigneeTemplate);
        taskTemplate.setEmailNotificationEnabled(false);

        int hashCode1 = taskTemplate.hashCode();
        int hashCode2 = taskTemplate.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void should_testHashCode_equalObjects() {
        TaskTemplateDefinition taskTemplate1 = new TaskTemplateDefinition();
        TaskTemplateDefinition taskTemplate2 = new TaskTemplateDefinition();

        TemplateDefinition assigneeTemplate = new TemplateDefinition(TemplateDefinition.TemplateType.VARIABLE, "assignee");
        TemplateDefinition candidateTemplate = new TemplateDefinition(TemplateDefinition.TemplateType.FILE, "candidate");

        taskTemplate1.setAssignee(assigneeTemplate);
        taskTemplate1.setCandidate(candidateTemplate);
        taskTemplate1.setEmailNotificationEnabled(true);

        taskTemplate2.setAssignee(assigneeTemplate);
        taskTemplate2.setCandidate(candidateTemplate);
        taskTemplate2.setEmailNotificationEnabled(true);

        assertThat(taskTemplate1).isEqualTo(taskTemplate2).hasSameHashCodeAs(taskTemplate2);
    }

    @Test
    void should_testHashCode_withNullFields() {
        TaskTemplateDefinition taskTemplate1 = new TaskTemplateDefinition();
        TaskTemplateDefinition taskTemplate2 = new TaskTemplateDefinition();

        taskTemplate1.setAssignee(null);
        taskTemplate1.setCandidate(null);
        taskTemplate1.setEmailNotificationEnabled(false);

        taskTemplate2.setAssignee(null);
        taskTemplate2.setCandidate(null);
        taskTemplate2.setEmailNotificationEnabled(false);

        assertThat(taskTemplate1).isEqualTo(taskTemplate2).hasSameHashCodeAs(taskTemplate2);
    }
}
