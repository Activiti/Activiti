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
package org.activiti.bpmn.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BpmnModelTest {

    private BpmnModel bpmnModel;

    @BeforeEach
    public void setUp() {
        bpmnModel = new BpmnModel();
    }

    @Test
    public void getErrorCode_shouldReturnNull_whenErrorRefIsNull() {
        assertThat(bpmnModel.getErrorCode(null)).isNull();
    }

    @Test
    public void getErrorCode_shouldReturnNull_whenErrorRefDoesNotExist() {
        assertThat(bpmnModel.getErrorCode("nonExistentErrorRef")).isNull();
    }

    @Test
    public void getErrorCode_shouldReturnCorrectErrorCode_whenMultipleErrorsExist() {
        bpmnModel.addError("errorRef1", "Error One", "CODE_001");
        bpmnModel.addError("errorRef2", "Error Two", "CODE_002");
        bpmnModel.addError("errorRef3", "Error Three", "CODE_003");

        assertThat(bpmnModel.getErrorCode("errorRef1")).isEqualTo("CODE_001");
        assertThat(bpmnModel.getErrorCode("errorRef2")).isEqualTo("CODE_002");
        assertThat(bpmnModel.getErrorCode("errorRef3")).isEqualTo("CODE_003");
    }

    @Test
    public void getErrorCode_shouldReturnNull_whenErrorExistsButHasNullErrorCode() {
        bpmnModel.addError("errorRef", "Error Name", null);

        assertThat(bpmnModel.getErrorCode("errorRef")).isNull();
    }

    @Test
    public void findErrorById_shouldReturnError_whenIdExists() {
        bpmnModel.addError("errorRef1", "Error One", "CODE_001");
        bpmnModel.addError("errorRef2", "Error Two", "CODE_002");

        assertThat(bpmnModel.findErrorById("errorRef2"))
            .isPresent()
            .get()
            .extracting(Error::getId, Error::getName, Error::getErrorCode)
            .containsExactly("errorRef2", "Error Two", "CODE_002");
    }

    @Test
    public void findErrorById_shouldReturnEmpty_whenIdDoesNotExist() {
        bpmnModel.addError("errorRef1", "Error One", "CODE_001");

        assertThat(bpmnModel.findErrorById("nonExistentErrorRef")).isEmpty();
    }

    @Test
    public void findErrorById_shouldReturnEmpty_whenIdIsNull() {
        bpmnModel.addError("errorRef1", "Error One", "CODE_001");

        assertThat(bpmnModel.findErrorById(null)).isEmpty();
    }

    @Test
    public void findErrorById_shouldReturnEmpty_whenModelHasNoErrors() {
        assertThat(bpmnModel.findErrorById("errorRef1")).isEmpty();
    }

    @Test
    public void findErrorByErrorCode_shouldReturnError_whenCodeExists() {
        bpmnModel.addError("errorRef1", "Error One", "CODE_001");
        bpmnModel.addError("errorRef2", "Error Two", "CODE_002");

        assertThat(bpmnModel.findErrorByErrorCode("CODE_002"))
            .isPresent()
            .get()
            .extracting(Error::getId, Error::getName, Error::getErrorCode)
            .containsExactly("errorRef2", "Error Two", "CODE_002");
    }

    @Test
    public void findErrorByErrorCode_shouldReturnEmpty_whenCodeDoesNotExist() {
        bpmnModel.addError("errorRef1", "Error One", "CODE_001");

        assertThat(bpmnModel.findErrorByErrorCode("UNKNOWN_CODE")).isEmpty();
    }

    @Test
    public void findErrorByErrorCode_shouldReturnEmpty_whenModelHasNoErrors() {
        assertThat(bpmnModel.findErrorByErrorCode("CODE_001")).isEmpty();
    }

    @Test
    public void findErrorByErrorCode_shouldReturnFirstError_whenMultipleErrorsShareSameCode() {
        bpmnModel.addError("errorRef1", "First", "SHARED_CODE");
        bpmnModel.addError("errorRef2", "Second", "SHARED_CODE");

        assertThat(bpmnModel.findErrorByErrorCode("SHARED_CODE")).get().extracting(Error::getId).isEqualTo("errorRef1");
    }

    @Test
    public void findErrorByErrorCode_shouldSkipErrorsWithNullErrorCode() {
        bpmnModel.addError("errorRef1", "No Code", null);
        bpmnModel.addError("errorRef2", "With Code", "CODE_002");

        assertThat(bpmnModel.findErrorByErrorCode("CODE_002")).get().extracting(Error::getId).isEqualTo("errorRef2");
    }
}
