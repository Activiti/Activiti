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
package org.activiti.spring.process.variable;

import org.activiti.common.util.DateFormatterProvider;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.spring.process.model.VariableDefinition;
import org.activiti.spring.process.variable.types.JsonObjectVariableType;
import org.activiti.spring.process.variable.types.VariableType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class VariableParsingServiceTest {

    @Autowired
    private VariableParsingService variableParsingService;

    @Autowired
    private Map<String, VariableType> variableTypeMap;

    @Autowired
    private DateFormatterProvider dateFormatterProvider;

    @MockBean
    private RepositoryService repositoryService;

    @Test
    void shouldParseBooleanVariable() {
        assertThat(variableParsingService.parse(new VariableDefinition("boolean", Boolean.TRUE))).isEqualTo(Boolean.TRUE);
    }

    @Test
    void shouldParseStringVariable() {
        String stringVar = "Han Solo";
        assertThat(variableParsingService.parse(new VariableDefinition("string", stringVar))).isEqualTo(stringVar);
    }

    @Test
    void shouldParseIntegerVariable() {
        Integer integerVar = 1;
        assertThat(variableParsingService.parse(new VariableDefinition("integer", integerVar))).isEqualTo(integerVar);
    }

    @Test
    void shouldParseBigdecimalVariable() {
        BigDecimal bigDecimal = BigDecimal.valueOf(2.3);
        assertThat(variableParsingService.parse(new VariableDefinition("bigdecimal", bigDecimal))).isEqualTo(bigDecimal);
    }

    @Test
    void shouldParseDateVariableFromDateObject() {

        Date dateVar = new Date();
        assertThat(variableParsingService.parse(new VariableDefinition("date", dateVar))).isEqualTo(dateVar);
    }
    @Test
    void shouldParseDateVariableFromString() {

        Date dateVar = new Date();
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(dateFormatterProvider.getDateFormatPattern()).toFormatter().withZone(dateFormatterProvider.getZoneId());
        String stringDate = formatter.format(dateVar.toInstant());

        assertThat(variableParsingService.parse(new VariableDefinition("date", stringDate))).isEqualTo(dateVar);
    }

    @Test
    void shouldParseDateVariableFromEpoch() {

        Date dateVar = new Date();
        assertThat(variableParsingService.parse(new VariableDefinition("date", dateVar.getTime()))).isEqualTo(dateVar);
    }

    @Test
    void shouldParseDatetimeVariableFromDateObject() {

        Date dateVar = new Date();
        assertThat(variableParsingService.parse(new VariableDefinition("datetime", dateVar))).isEqualTo(dateVar);
    }
    @Test
    void shouldParseDatetimeVariableFromString() {

        Date dateVar = new Date();
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(dateFormatterProvider.getDateFormatPattern()).toFormatter().withZone(dateFormatterProvider.getZoneId());
        String stringDate = formatter.format(dateVar.toInstant());

        assertThat(variableParsingService.parse(new VariableDefinition("datetime", stringDate))).isEqualTo(dateVar);
    }

    @Test
    void shouldParseDatetimeVariableFromEpoch() {

        Date dateVar = new Date();
        assertThat(variableParsingService.parse(new VariableDefinition("date", dateVar.getTime()))).isEqualTo(dateVar);
    }

    @Test
    void should_ReturnSameObject_whenAssignedVariableTypeIsJsonObjectVariableType() {

        List<String> jsonObjectVariableTypes = variableTypeMap.entrySet().stream().filter(e -> e.getValue().equals(JsonObjectVariableType.class)).map(Map.Entry::getKey).toList();
        assertThat(jsonObjectVariableTypes).allSatisfy(type -> {
            Object obj = new Object();
            assertThat(variableParsingService.parse(new VariableDefinition(type, obj))).isEqualTo(obj);
        });
    }

    @Test
    void should_ThrowActivitiException_whenParsingBigdecimalFromInvalidValue() {
        assertThatThrownBy(() -> variableParsingService.parse(new VariableDefinition("bigdecimal","Michael Jackson"))).isInstanceOf(ActivitiException.class);
    }

    @Test
    void should_ThrowActivitiException_whenParsingDateFromInvalidValue() {
        assertThatThrownBy(() -> variableParsingService.parse(new VariableDefinition("date","Michael Jackson"))).isInstanceOf(ActivitiException.class);
    }

}
