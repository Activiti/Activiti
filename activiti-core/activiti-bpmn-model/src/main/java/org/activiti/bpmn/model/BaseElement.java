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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public abstract class BaseElement implements HasExtensionAttributes {

    protected String id;
    protected int xmlRowNumber;
    protected int xmlColumnNumber;
    protected Map<String, List<ExtensionElement>> extensionElements = new LinkedHashMap<String, List<ExtensionElement>>();
    /**
     * extension attributes could be part of each element
     */
    protected Map<String, List<ExtensionAttribute>> attributes = new LinkedHashMap<String, List<ExtensionAttribute>>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getXmlRowNumber() {
        return xmlRowNumber;
    }

    public void setXmlRowNumber(int xmlRowNumber) {
        this.xmlRowNumber = xmlRowNumber;
    }

    public int getXmlColumnNumber() {
        return xmlColumnNumber;
    }

    public void setXmlColumnNumber(int xmlColumnNumber) {
        this.xmlColumnNumber = xmlColumnNumber;
    }

    public Map<String, List<ExtensionElement>> getExtensionElements() {
        return extensionElements;
    }

    public void addExtensionElement(ExtensionElement extensionElement) {
        if (extensionElement != null && isNotEmpty(extensionElement.getName())) {
            extensionElements.computeIfAbsent(extensionElement.getName(), k -> new ArrayList<>());
            this.extensionElements.get(extensionElement.getName()).add(extensionElement);
        }
    }

    public void setExtensionElements(Map<String, List<ExtensionElement>> extensionElements) {
        this.extensionElements = extensionElements;
    }

    @Override
    public Map<String, List<ExtensionAttribute>> getAttributes() {
        return attributes;
    }

    @Override
    public String getAttributeValue(String namespace, String name) {

        return Optional.ofNullable(getAttributes())
                .map(map -> map.get(name))
                .orElse(Collections.emptyList()).stream()
                .filter(e -> this.isNamespaceMatching(namespace, e))
                .findFirst().map(ExtensionAttribute::getValue).orElse(null);
    }

    private boolean isNamespaceMatching(String namespace, ExtensionAttribute attribute) {
        return (namespace == null && attribute.getNamespace() == null)
                || (namespace != null && namespace.equals(attribute.getNamespace()));
    }

    @Override
    public void addAttribute(ExtensionAttribute attribute) {
        if (attribute != null && isNotEmpty(attribute.getName())) {
            attributes.computeIfAbsent(attribute.getName(), key -> new ArrayList<>());
            attributes.get(attribute.getName()).add(attribute);
        }
    }

    @Override
    public void setAttributes(Map<String, List<ExtensionAttribute>> attributes) {
        this.attributes = attributes;
    }

    public void setValues(BaseElement otherElement) {
        setId(otherElement.getId());

        if (otherElement.getExtensionElements() != null && !otherElement.getExtensionElements().isEmpty()) {
            Map<String, List<ExtensionElement>> validExtensionElements = otherElement.getExtensionElements().entrySet()
                    .stream().filter(e -> hasElements(e.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            extensionElements.putAll(validExtensionElements);
        }

        if (otherElement.getAttributes() != null && !otherElement.getAttributes().isEmpty()) {
            Map<String, List<ExtensionAttribute>> validAttributes = otherElement.getAttributes().entrySet().stream()
                    .filter(e -> hasElements(e.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            attributes.putAll(validAttributes);
        }
    }

    private boolean hasElements(List<?> listOfElements) {
        return listOfElements != null && !listOfElements.isEmpty();
    }

    public abstract BaseElement clone();
}
