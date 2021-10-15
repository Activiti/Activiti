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

package org.activiti.editor.language.json.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ServiceTask;

/**

 */
public class MuleTaskJsonConverter extends BaseBpmnJsonConverter {

    public static void fillTypes(
        Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
        Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap
    ) {
        fillJsonTypes(convertersToBpmnMap);
        fillBpmnTypes(convertersToJsonMap);
    }

    public static void fillJsonTypes(
        Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap
    ) {
        convertersToBpmnMap.put(STENCIL_TASK_MULE, MuleTaskJsonConverter.class);
    }

    public static void fillBpmnTypes(
        Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap
    ) {}

    protected String getStencilId(BaseElement baseElement) {
        return STENCIL_TASK_MULE;
    }

    protected void convertElementToJson(
        ObjectNode propertiesNode,
        BaseElement baseElement
    ) {
        // done in service task
    }

    protected FlowElement convertJsonToElement(
        JsonNode elementNode,
        JsonNode modelNode,
        Map<String, JsonNode> shapeMap
    ) {
        ServiceTask task = new ServiceTask();
        task.setType("mule");
        addField(
            "endpointUrl",
            PROPERTY_MULETASK_ENDPOINT_URL,
            elementNode,
            task
        );
        addField("language", PROPERTY_MULETASK_LANGUAGE, elementNode, task);
        addField(
            "payloadExpression",
            PROPERTY_MULETASK_PAYLOAD_EXPRESSION,
            elementNode,
            task
        );
        addField(
            "resultVariable",
            PROPERTY_MULETASK_RESULT_VARIABLE,
            elementNode,
            task
        );
        return task;
    }
}
