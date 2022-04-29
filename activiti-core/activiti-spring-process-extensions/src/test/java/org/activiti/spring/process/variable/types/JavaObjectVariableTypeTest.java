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
import org.activiti.engine.ActivitiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JavaObjectVariableTypeTest {

    private List<ActivitiException> exceptionList;
    JavaObjectVariableType javaObjectVariableType;

    @BeforeEach
    public void setUp() {
        javaObjectVariableType = new JavaObjectVariableType(Boolean.class);
        exceptionList = new ArrayList<>();
    }

    @Test
    public void should_returnException_when_validateValueNotAssignableToClass() {
        javaObjectVariableType.validate(1, exceptionList);

        assertTrue(exceptionList.stream().anyMatch(error ->
            error.getMessage().equals("class java.lang.Integer is not assignable from class java.lang.Boolean")
        ));
    }

    @Test
    public void should_returnEmptyErrorList_when_validateValueAssignableToClass() {
        javaObjectVariableType.validate(true, exceptionList);

        assertTrue(exceptionList.isEmpty());
    }

    @Test
    public void should_returnEmptyErrorList_when_validateValidExpression() {
        javaObjectVariableType.validate("${now()}", exceptionList);

        assertTrue(exceptionList.isEmpty());
    }

    @Test
    public void should_returnException_when_validateIncompleteExpression() {
        javaObjectVariableType.validate("${now()", exceptionList);

        assertTrue(exceptionList.stream().anyMatch(error ->
            error.getMessage().equals("class java.lang.String is not assignable from class java.lang.Boolean")
        ));
    }
}
