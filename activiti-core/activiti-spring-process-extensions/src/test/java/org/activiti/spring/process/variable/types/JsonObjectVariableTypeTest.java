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
package org.activiti.spring.process.variable.types;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.ActivitiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JsonObjectVariableTypeTest {

    private List<ActivitiException> exceptionList;
    JsonObjectVariableType jsonObjectVariableType;

    @BeforeEach
    public void setUp() {
        jsonObjectVariableType = new JsonObjectVariableType(new ObjectMapper());
        exceptionList = new ArrayList<>();
    }

    @Test
    public void should_returnException_when_validateValueNotAssignableToClass() {
        jsonObjectVariableType.validate("${now()}", exceptionList);

        assertTrue(exceptionList.isEmpty());
    }

}
