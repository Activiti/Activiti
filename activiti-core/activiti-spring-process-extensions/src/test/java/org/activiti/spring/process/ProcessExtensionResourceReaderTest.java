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
package org.activiti.spring.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

public class ProcessExtensionResourceReaderTest {

    @InjectMocks
    private ProcessExtensionResourceReader reader;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void shouldSelectFileWithSuffixHyphenExtensionsDotJson() {
        assertThat(reader.getResourceNameSelector().test("any-path/to/my-extension/my-process-extensions.json"))
                .isTrue();
    }

    @Test
    public void shouldNotSelectSelectJsonFileWithoutSuffixHyphenExtensionsDotJson() {
        assertThat(reader.getResourceNameSelector().test("any-path/to/my-extension/my-process-other.json"))
                .isFalse();
    }
}
