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
package org.activiti.bpmn.model;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class BaseElementTest {

    private BaseElement baseElement;

    @Before
    public void setUp() {
        this.baseElement = new BaseElement() {
            @Override
            public BaseElement clone() {
                return null;
            }
        };
    }

    @Test
    public void testAddExtensionElement() {

        //Given
        ExtensionElement successExtensionElement = new ExtensionElement();
        successExtensionElement.setName("success");
        successExtensionElement.setNamespacePrefix("prefix");
        successExtensionElement.setNamespace("namespace");

        ExtensionElement emptyExtensionElement = new ExtensionElement();

        //When
        baseElement.addExtensionElement(successExtensionElement);
        baseElement.addExtensionElement(emptyExtensionElement);

        //Then
        assertThat(baseElement.getExtensionElements()).hasSize(1);

        assertThat(baseElement.getExtensionElements().get("success"))
                .isNotNull()
                .containsExactly(successExtensionElement);

    }

    @Test
    public void testGetAttributeValue() {
        //Given
        ExtensionAttribute extensionAttributeOne = createExtensionAttribute("attrOne", "attrValueOne");

        ExtensionAttribute extensionAttributeTwo = createExtensionAttribute("attrTwo", "attrValueTwo");
        extensionAttributeTwo.setNamespace(null);

        ExtensionAttribute extensionAttributeThree = createExtensionAttribute("attrThree", "attrValueTwo");


        baseElement.getAttributes().put("attributeOne", asList(
                extensionAttributeOne,
                extensionAttributeTwo,
                extensionAttributeThree
        ));

        //When
        String successValue = baseElement.getAttributeValue("namespace", "attributeOne");
        String nonNamespace = baseElement.getAttributeValue(null, "attributeOne");
        String nonAttribute = baseElement.getAttributeValue("namespace", "other");

        //Then
        assertThat(successValue).isEqualTo("attrValueOne");
        assertThat(nonNamespace).isEqualTo("attrValueTwo");
        assertThat(nonAttribute).isNull();
    }

    @Test
    public void testAddAttribute() {

        //Given
        ExtensionAttribute attributeOne = createExtensionAttribute("attr", "valueOne");

        ExtensionAttribute attributeTwo = createExtensionAttribute("attr", "valueTwo");

        //When and Then
        baseElement.addAttribute(attributeOne);
        assertThat(baseElement.getAttributes()).hasSize(1);
        assertThat(baseElement.getAttributes().get("attr")).hasSize(1).containsExactly(attributeOne);

        //When and Then
        baseElement.addAttribute(attributeTwo);
        assertThat(baseElement.getAttributes().get("attr")).hasSize(2).containsExactly(attributeOne, attributeTwo);
        assertThat(baseElement.getAttributes()).hasSize(1);
    }


    @Test
    public void testSetValues() {
        //Given
        BaseElement otherBaseElement = createOtherBaseElement();

        //When
        baseElement.setValues(otherBaseElement);

        //Then
        assertThat(baseElement.getId()).isEqualTo("otherBaseElementId");
        assertThat(baseElement.getExtensionElements()).hasSize(1);

        List<ExtensionElement> extensionElements = baseElement.getExtensionElements().get("elementOne");
        assertThat(extensionElements).hasSize(1);

        ExtensionElement extensionElementOne = extensionElements.get(0);
        assertThat(extensionElementOne.getName()).isEqualTo("elementOne");
        assertThat(extensionElementOne.getAttributes()).hasSize(1);

        List<ExtensionAttribute> nestedAttributesInElementOne = extensionElementOne.getAttributes().get("attrOneElementOne");
        assertThat(nestedAttributesInElementOne).hasSize(1);
        assertThat(nestedAttributesInElementOne.get(0).getName()).isEqualTo("attrOneElementOne");
        assertThat(nestedAttributesInElementOne.get(0).getValue()).isEqualTo("attrOneElementOneValue");

        assertThat(baseElement.getAttributes()).hasSize(1);
        assertThat(baseElement.getAttributeValue("namespace", "attrOne")).isEqualTo("attrValueOne");
    }

    private BaseElement createOtherBaseElement() {
        BaseElement baseElement = new BaseElement() {
            @Override
            public BaseElement clone() {
                return null;
            }
        };

        baseElement.setId("otherBaseElementId");

        ExtensionAttribute extensionAttributeElementOne =
                createExtensionAttribute("attrOneElementOne", "attrOneElementOneValue");

        ExtensionElement extensionElementOne = createExtensionElement(
                "elementOne",
                singletonList(extensionAttributeElementOne)
        );

        ExtensionAttribute extensionAttributeOne = createExtensionAttribute("attrOne", "prefix", "attrValueOne");

        baseElement.addExtensionElement(extensionElementOne);
        baseElement.addAttribute(extensionAttributeOne);
        return baseElement;
    }

    private ExtensionElement createExtensionElement(String name, List<ExtensionAttribute> extensionAttributes) {
        ExtensionElement extensionElement = new ExtensionElement();
        extensionElement.setName(name);
        extensionElement.setNamespace("namespace");
        Optional.ofNullable(extensionAttributes).orElse(Collections.emptyList()).forEach(extensionElement::addAttribute);
        return extensionElement;
    }

    private ExtensionAttribute createExtensionAttribute(String name, String value) {
        return createExtensionAttribute(name, null, value);
    }

    private ExtensionAttribute createExtensionAttribute(String name, String prefix, String value) {
        ExtensionAttribute extensionAttributeOne = new ExtensionAttribute();
        extensionAttributeOne.setName(name);
        extensionAttributeOne.setNamespacePrefix(prefix);
        extensionAttributeOne.setNamespace("namespace");
        extensionAttributeOne.setValue(value);
        return extensionAttributeOne;
    }

}
