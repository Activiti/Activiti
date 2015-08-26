/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.rest.editor;

import javax.inject.Inject;

import org.activiti.bpmn.model.GraphicInfo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.editor.Model;
import com.activiti.domain.editor.ModelHistory;
import com.activiti.service.editor.BpmnDisplayJsonConverter;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
public class EditorDisplayJsonClientResource extends BaseModelResource {

	@Inject
	protected BpmnDisplayJsonConverter bpmnDisplayJsonConverter;
	
	protected ObjectMapper objectMapper = new ObjectMapper();
	
	@RequestMapping(value = "/rest/models/{processModelId}/model-json", method = RequestMethod.GET, produces = "application/json")
	@Timed
	public JsonNode getModelJSON(@PathVariable Long processModelId) {
		ObjectNode displayNode = objectMapper.createObjectNode();
		Model model = getModel(processModelId, true, false);
		bpmnDisplayJsonConverter.processProcessElements(model, displayNode, new GraphicInfo());
		return displayNode;
	}
	
	@RequestMapping(value = "/rest/models/{processModelId}/history/{processModelHistoryId}/model-json", method = RequestMethod.GET, produces = "application/json")
    @Timed
    public JsonNode getModelHistoryJSON(@PathVariable Long processModelId, @PathVariable Long processModelHistoryId) {
	    ObjectNode displayNode = objectMapper.createObjectNode();
        ModelHistory model = getModelHistory(processModelId, processModelHistoryId, true, false);
        bpmnDisplayJsonConverter.processProcessElements(model, displayNode, new GraphicInfo());
        return displayNode;
    }
}
