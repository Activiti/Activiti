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
package org.activiti.spring.resolver;

import jakarta.el.ELContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SystemStubsExtension.class)
class EnvironmentVariableELResolverTest {

    private ELContext elContext;

    private EnvironmentVariableELResolver resolver;

    @BeforeEach
    void setUp() {
        elContext = Mockito.mock(ELContext.class);
        resolver = new EnvironmentVariableELResolver();
    }

    @Test
    void should_returnEnvVar_when_baseIsNull_and_propertyIsVars() {
        // Arrange
        String property = "vars";

        // Act
        Object result = resolver.getValue(elContext, null, property);

        // Assert
        assertThat(result).isInstanceOf(EnvVar.class);
        Mockito.verify(elContext).setPropertyResolved(true);
    }

    @Test
    void should_getValue_with_validEnvVariable() throws Exception {
        // Arrange
        EnvironmentVariables environmentVariables = new EnvironmentVariables("vars.MY_ENV_VAR", "test-value");
        environmentVariables.setup();

        // Act
        var result = resolver.getValue(elContext,new EnvVar(), "MY_ENV_VAR");

        // Assert
        assertThat(result).isEqualTo("test-value");

        environmentVariables.teardown();
    }

    @Test
    void should_returnNull_when_invalidEnvVariable() {
        // Arrange
        String variableName = "nonExistentVar";

        // Act
        Object value = resolver.getValue(elContext, null, variableName);

        // Assert
        assertThat(value).isNull();
    }

    @Test
    void should_getType_as_String() {
        // Arrange
        String variableName = "vars.testVar";

        // Act
        Class<?> type = resolver.getType(elContext, null, variableName);

        // Assert
        assertThat(type).isEqualTo(String.class);
    }

    @Test
    void should_getUnsupportedOperationException_when_setValue() {
        // Arrange
        String variableName = "vars.testVar";

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            resolver.setValue(elContext, null, variableName, "newValue");
        });
    }

    @Test
    void should_isReadOnly_returnTrue() {
        // Arrange
        String variableName = "vars.testVar";

        // Act
        boolean isReadOnly = resolver.isReadOnly(elContext, null, variableName);

        // Assert
        assertThat(isReadOnly).isTrue();
    }

    @Test
    void should_getString_when_getCommonPropertyType() {
        // Act
        Class<?> commonPropertyType = resolver.getCommonPropertyType(elContext, null);

        // Assert
        assertThat(commonPropertyType).isEqualTo(String.class);
    }
}
