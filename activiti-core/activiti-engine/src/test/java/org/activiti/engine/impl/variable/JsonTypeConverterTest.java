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
package org.activiti.engine.impl.variable;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class JsonTypeConverterTest {

    private static final String TYPE_PROPERTY_NAME = "@class";

    private ObjectMapper objectMapper;
    private JsonTypeConverter converter;

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        converter = new JsonTypeConverter(objectMapper, TYPE_PROPERTY_NAME);
    }

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

    @Test
    public void should_convertLocalDateTime() throws Exception {
        //given
        LocalDateTime localDateTime = LocalDateTime.parse("2020-08-12T12:00", DateTimeFormatter.ISO_DATE_TIME);
        String json = objectMapper.writeValueAsString(localDateTime.toString());

        JsonNode jsonNode = objectMapper.readTree(json);

        //when
        Object convertedValue = converter.convertToValue(jsonNode, buildValueFields("localDateTime", localDateTime));

        //then
        assertThat(convertedValue).isInstanceOf(LocalDateTime.class);
        assertThat(((LocalDateTime) convertedValue).toString()).isEqualTo("2020-08-12T12:00");
    }

    @Test
    public void should_convertLocalDate() throws Exception {
        //given
        LocalDate localDate = LocalDate.parse("2020-08-12", DateTimeFormatter.ISO_DATE);
        String json = objectMapper.writeValueAsString(localDate.toString());

        JsonNode jsonNode = objectMapper.readTree(json);

        //when
        Object convertedValue = converter.convertToValue(jsonNode, buildValueFields("localDate", localDate));

        //then
        assertThat(convertedValue).isInstanceOf(LocalDate.class);
        assertThat(((LocalDate) convertedValue).toString()).isEqualTo("2020-08-12");
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
