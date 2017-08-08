/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.services.audit.converter;

import java.util.Date;

import org.activiti.services.audit.events.model.ProcessInstance;
import org.junit.Test;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.activiti.test.Assertions.assertThat;

public class ProcessInstanceJpaJsonConverterTest {

    private ProcessInstanceJpaJsonConverter converter = new ProcessInstanceJpaJsonConverter();

    @Test
    public void convertToDatabaseColumnShouldReturnTheEntityJsonRepresentation() throws Exception {
        //given
        ProcessInstance processInstance = new ProcessInstance("20",
                                                              "My instance",
                                                              "This is my process instance",
                                                              "proc-def-id",
                                                              "initiator",
                                                              new Date(),
                                                              "business-key",
                                                              "running");

        //when
        String jsonRepresentation = converter.convertToDatabaseColumn(processInstance);

        //then
        assertThatJson(jsonRepresentation)
                .node("name").isEqualTo("My instance")
                .node("status").isEqualTo("running")
                .node("processDefinitionId").isEqualTo("proc-def-id")
                .node("businessKey").isEqualTo("business-key")
                .node("id").isEqualTo("\"20\"");
    }

    @Test
    public void convertToEntityAttributeShouldCreateAProcessInstanceWithFieldsSet() throws Exception {
        //given
        String jsonRepresentation =
                "{\"id\":\"20\"," +
                        "\"status\":\"running\"," +
                        "\"name\":\"My instance\"," +
                        "\"processDefinitionId\":\"proc-def-id\"}";

        //when
        ProcessInstance processInstance = converter.convertToEntityAttribute(jsonRepresentation);

        //then
        assertThat(processInstance)
                .isNotNull()
                .hasId("20")
                .hasStatus("running")
                .hasProcessDefinitionId("proc-def-id");
    }
}