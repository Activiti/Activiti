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
package org.activiti.editor.language;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.InputStream;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.junit.jupiter.api.Test;

public class BpmnJsonConverterNamespaceTest {

    private ObjectNode buildModelNode() throws Exception {
        InputStream jsonStream = getClass().getClassLoader().getResourceAsStream("test.simplemodel.json");
        JsonNode modelNode = new ObjectMapper().readTree(jsonStream);
        return (ObjectNode) modelNode;
    }

    @Test
    public void testConvertToBpmnModel_noNamespace_targetNamespaceShouldBeNull() throws Exception {
        ObjectNode modelNode = buildModelNode();
        ((ObjectNode) modelNode.get("properties")).remove("process_namespace");

        BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(modelNode);

        assertThat(bpmnModel.getTargetNamespace()).isNull();
    }

    @Test
    public void testConvertToBpmnModel_emptyNamespace_targetNamespaceShouldBeNull() throws Exception {
        ObjectNode modelNode = buildModelNode();
        ((ObjectNode) modelNode.get("properties")).put("process_namespace", "");

        BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(modelNode);

        assertThat(bpmnModel.getTargetNamespace()).isNull();
    }

    @Test
    public void testConvertToBpmnModel_explicitNamespace_targetNamespaceShouldBePreserved() throws Exception {
        ObjectNode modelNode = buildModelNode();
        ((ObjectNode) modelNode.get("properties")).put("process_namespace", "http://mycompany.org/processes");

        BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(modelNode);

        assertThat(bpmnModel.getTargetNamespace()).isEqualTo("http://mycompany.org/processes");
    }
}
