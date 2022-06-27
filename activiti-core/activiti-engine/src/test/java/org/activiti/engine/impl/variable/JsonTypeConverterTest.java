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
package org.activiti.engine.impl.variable;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.Test;

public class JsonTypeConverterTest {

    private static final String TYPE_PROPERTY_NAME = "@class";
    private static ObjectMapper objectMapper = new ObjectMapper();

    private JsonTypeConverter converter = new JsonTypeConverter(objectMapper, TYPE_PROPERTY_NAME);

    @Test
    public void should_convertToList() throws Exception {
        //given
        List<Integer> originalValue = asList(1, 2);
        String json = objectMapper.writeValueAsString(originalValue);
        JsonNode jsonNode = objectMapper.readTree(json);
        System.out.println(json);

        ValueFields valueFields = buildValueFields("numbers", originalValue);

        //when
        Object numbers = converter.convertToValue(jsonNode, valueFields);

        //then
        assertThat(numbers).isInstanceOf(List.class);
        assertThat(((List<?>) numbers).get(0)).isInstanceOf(Integer.class);
        assertThat(numbers).isEqualTo(originalValue);
    }

    private ValueFields buildValueFields(String name, Object value) {
        ValueFields valueFields = mock(ValueFields.class);
        given(valueFields.getName()).willReturn(name);
        given(valueFields.getTextValue2()).willReturn(value.getClass().getName());
        return valueFields;
    }

    @Test
    public void should_convertToPOJO() throws Exception {
        //given
        Person person = new Person("John", "Doe");
        String json = objectMapper.writeValueAsString(person);
        JsonNode jsonNode = objectMapper.readTree(json);

        //when
        Object convertedValue = converter.convertToValue(jsonNode, buildValueFields("person", person));

        //then
        assertThat(convertedValue).isInstanceOf(Person.class);
        assertThat(((Person) convertedValue).getFirstName()).isEqualTo("John");
        assertThat(((Person) convertedValue).getLastName()).isEqualTo("Doe");
    }

    @JsonTypeInfo(property = TYPE_PROPERTY_NAME, use = Id.CLASS)
    private static class Person {

        private String firstName;
        private String lastName;

        public Person() {
        }

        public Person(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
    }

}
