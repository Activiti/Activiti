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
package org.activiti.api.runtime.test.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.api.runtime.model.impl.ExternalizedContextReferenceImpl;
import org.junit.jupiter.api.Test;

class ExternalizedContextReferenceImplTest {

    @Test
    void should_setPropertiesThroughConstructor() {
        ExternalizedContextReferenceImpl reference = new ExternalizedContextReferenceImpl(
            "providerType",
            "http://example.com"
        );

        assertThat(reference.getProviderType()).isEqualTo("providerType");
        assertThat(reference.getUrl()).isEqualTo("http://example.com");
    }

    @Test
    void should_setPropertiesThroughSetters() {
        ExternalizedContextReferenceImpl reference = new ExternalizedContextReferenceImpl();

        reference.setProviderType("providerType");
        reference.setUrl("http://example.com");

        assertThat(reference.getProviderType()).isEqualTo("providerType");
        assertThat(reference.getUrl()).isEqualTo("http://example.com");
    }

    @Test
    void should_beEqual_when_propertiesMatch() {
        ExternalizedContextReferenceImpl first = new ExternalizedContextReferenceImpl(
            "providerType",
            "http://example.com"
        );
        ExternalizedContextReferenceImpl second = new ExternalizedContextReferenceImpl(
            "providerType",
            "http://example.com"
        );

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);
    }

    @Test
    void should_notBeEqual_when_propertiesDiffer() {
        ExternalizedContextReferenceImpl first = new ExternalizedContextReferenceImpl(
            "providerType",
            "http://example.com"
        );
        ExternalizedContextReferenceImpl second = new ExternalizedContextReferenceImpl(
            "otherProviderType",
            "http://other.com"
        );

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void should_includePropertiesInToString() {
        ExternalizedContextReferenceImpl reference = new ExternalizedContextReferenceImpl(
            "providerType",
            "http://example.com"
        );

        String toString = reference.toString();

        assertThat(toString).contains("providerType");
        assertThat(toString).contains("http://example.com");
    }
}
