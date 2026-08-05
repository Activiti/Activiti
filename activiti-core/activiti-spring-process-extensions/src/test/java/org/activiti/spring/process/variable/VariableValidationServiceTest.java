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
package org.activiti.spring.process.variable;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.common.util.DateFormatterProvider;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.spring.process.model.VariableDefinition;
import org.activiti.spring.process.variable.types.BigDecimalVariableType;
import org.activiti.spring.process.variable.types.DateVariableType;
import org.activiti.spring.process.variable.types.JavaObjectVariableType;
import org.activiti.spring.process.variable.types.VariableType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
class VariableValidationServiceTest {

    @Autowired
    private Map<String, VariableType> variableTypeMap;

    @Autowired
    private VariableValidationService variableValidationService;

    @MockitoBean
    private RepositoryService repositoryService;

    @Test
    void should_returnEmptyErrors_when_validatingNullValueDirectlyForEveryRegisteredType() {
        assertThat(variableTypeMap).hasSizeGreaterThanOrEqualTo(11);

        variableTypeMap.forEach((typeKey, variableType) -> {
            List<ActivitiException> errors = new ArrayList<>();
            variableType.validate(null, errors);
            assertThat(errors).as("null validation for type '%s'", typeKey).isEmpty();
        });
    }

    @Test
    void should_returnEmptyErrors_when_validatingNullValueThroughServiceForEveryRegisteredType() {
        assertThat(variableTypeMap).hasSizeGreaterThanOrEqualTo(11);

        variableTypeMap
            .keySet()
            .forEach(typeKey -> {
                VariableDefinition definition = definitionOfType("var-" + typeKey, typeKey);
                assertThat(variableValidationService.validateWithErrors(null, definition))
                    .as("null validation through service for type '%s'", typeKey)
                    .isEmpty();
            });
    }

    @Test
    void should_returnEmptyErrors_when_validatingNullValueThroughServiceForUnknownType() {
        VariableDefinition definition = definitionOfType("customVar", "unknown-custom-type");

        assertThat(variableValidationService.validateWithErrors(null, definition)).isEmpty();
    }

    @Test
    void should_returnHasNoTypeError_when_definitionHasNoType() {
        VariableDefinition definition = new VariableDefinition();
        definition.setName("untypedVar");

        List<ActivitiException> errors = variableValidationService.validateWithErrors(null, definition);

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getMessage()).isEqualTo("untypedVar has no type");
    }

    @Test
    void should_notInvokeTypeValidator_when_nullValueShortCircuitsInService() {
        ThrowingVariableType throwingType = new ThrowingVariableType();
        Map<String, VariableType> mapWithStub = new HashMap<>(variableTypeMap);
        mapWithStub.put("throwing-type", throwingType);

        VariableValidationService service = new VariableValidationService(mapWithStub);
        VariableDefinition definition = definitionOfType("stubVar", "throwing-type");

        assertThat(service.validateWithErrors(null, definition)).isEmpty();
    }

    @Test
    void should_returnEmptyErrors_when_unknownTypeHasNullValueAndJsonFallbackIsUnavailable() {
        DateFormatterProvider dateFormatterProvider = new DateFormatterProvider("yyyy-MM-dd[['T']HH:mm:ss[.SSS'Z']]");
        Map<String, VariableType> mapWithoutJson = Map.of(
            "boolean",
            new JavaObjectVariableType(Boolean.class),
            "string",
            new JavaObjectVariableType(String.class),
            "integer",
            new JavaObjectVariableType(Integer.class),
            "bigdecimal",
            new BigDecimalVariableType(),
            "date",
            new DateVariableType(java.util.Date.class, dateFormatterProvider),
            "datetime",
            new DateVariableType(java.util.Date.class, dateFormatterProvider)
        );

        VariableValidationService service = new VariableValidationService(mapWithoutJson);
        VariableDefinition definition = definitionOfType("unknownVar", "unknown-custom-type");

        assertThat(service.validateWithErrors(null, definition)).isEmpty();
    }

    private static VariableDefinition definitionOfType(String name, String type) {
        VariableDefinition definition = new VariableDefinition();
        definition.setName(name);
        definition.setType(type);
        return definition;
    }

    private static class ThrowingVariableType extends VariableType {

        @Override
        public void validate(Object value, List<ActivitiException> errors) {
            throw new RuntimeException("validate should not be called for null");
        }
    }
}
