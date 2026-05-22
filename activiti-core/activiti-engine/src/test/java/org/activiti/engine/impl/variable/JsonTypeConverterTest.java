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
package org.activiti.engine.impl.variable;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import java.util.List;
import org.junit.jupiter.api.Test;

public class JsonTypeConverterTest {

    private static final String TYPE_PROPERTY_NAME = "@class";
    private static JsonMapper jsonMapper = new JsonMapper();

    private JsonTypeConverter converter = new JsonTypeConverter(jsonMapper, TYPE_PROPERTY_NAME);

    @Test
    public void should_convertToList() throws Exception {
        //given
        List<Integer> originalValue = asList(1, 2);
        String json = jsonMapper.writeValueAsString(originalValue);
        JsonNode jsonNode = jsonMapper.readTree(json);
        IO.println(json);

        ValueFields valueFields = buildValueFields("numbers", originalValue);

        //when
        Object numbers = converter.convertToValue(jsonNode, valueFields);

        //then
        assertThat(numbers).isInstanceOf(List.class);
        assertThat(((List<?>) numbers).getFirst()).isInstanceOf(Integer.class);
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
        String json = jsonMapper.writeValueAsString(person);
        JsonNode jsonNode = jsonMapper.readTree(json);

        //when
        Object convertedValue = converter.convertToValue(jsonNode, buildValueFields("person", person));

        //then
        assertThat(convertedValue).isInstanceOf(Person.class);
        assertThat(((Person) convertedValue).getFirstName()).isEqualTo("John");
        assertThat(((Person) convertedValue).getLastName()).isEqualTo("Doe");
    }

    @Test
    public void should_returnArrayNode_whenTextValue2IsArrayNodeClassName() throws Exception {
        //given
        ArrayNode originalArray = jsonMapper.createArrayNode();
        originalArray.add(1);
        originalArray.add(2);
        JsonNode jsonNode = jsonMapper.readTree(jsonMapper.writeValueAsString(originalArray));

        ValueFields valueFields = mock(ValueFields.class);
        given(valueFields.getName()).willReturn("items");
        given(valueFields.getTextValue2()).willReturn("tools.jackson.databind.node.ArrayNode");

        //when
        Object result = converter.convertToValue(jsonNode, valueFields);

        //then
        assertThat(result).isInstanceOf(ArrayNode.class);
        assertThat(result).isEqualTo(originalArray);
    }

    @Test
    public void should_returnArrayNode_whenTextValue2IsLegacyJackson2ArrayNodeClassName() throws Exception {
        //given
        ArrayNode originalArray = jsonMapper.createArrayNode();
        originalArray.add("a");
        originalArray.add("b");
        JsonNode jsonNode = jsonMapper.readTree(jsonMapper.writeValueAsString(originalArray));

        ValueFields valueFields = mock(ValueFields.class);
        given(valueFields.getName()).willReturn("items");
        given(valueFields.getTextValue2()).willReturn("com.fasterxml.jackson.databind.node.ArrayNode");

        //when
        Object result = converter.convertToValue(jsonNode, valueFields);

        //then
        assertThat(result).isInstanceOf(ArrayNode.class);
        assertThat(result).isEqualTo(originalArray);
    }

    @Test
    public void should_returnObjectNode_whenTextValue2IsObjectNodeClassName() throws Exception {
        //given
        ObjectNode originalObject = jsonMapper.createObjectNode();
        originalObject.put("key", "value");
        JsonNode jsonNode = jsonMapper.readTree(jsonMapper.writeValueAsString(originalObject));

        ValueFields valueFields = mock(ValueFields.class);
        given(valueFields.getName()).willReturn("data");
        given(valueFields.getTextValue2()).willReturn("tools.jackson.databind.node.ObjectNode");

        //when
        Object result = converter.convertToValue(jsonNode, valueFields);

        //then
        assertThat(result).isInstanceOf(ObjectNode.class);
        assertThat(result).isEqualTo(originalObject);
    }

    @Test
    public void should_returnObjectNode_whenTextValue2IsLegacyJackson2ObjectNodeClassName() throws Exception {
        //given
        ObjectNode originalObject = jsonMapper.createObjectNode();
        originalObject.put("foo", 42);
        JsonNode jsonNode = jsonMapper.readTree(jsonMapper.writeValueAsString(originalObject));

        ValueFields valueFields = mock(ValueFields.class);
        given(valueFields.getName()).willReturn("data");
        given(valueFields.getTextValue2()).willReturn("com.fasterxml.jackson.databind.node.ObjectNode");

        //when
        Object result = converter.convertToValue(jsonNode, valueFields);

        //then
        assertThat(result).isInstanceOf(ObjectNode.class);
        assertThat(result).isEqualTo(originalObject);
    }

    @JsonTypeInfo(property = TYPE_PROPERTY_NAME, use = Id.CLASS)
    private static class Person {

        private String firstName;
        private String lastName;

        public Person() {}

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
