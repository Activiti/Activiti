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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jbarrez
 */
@RestController
public class ModelBpmnResource extends AbstractModelBpmnResource {

    /**
     * GET /rest/models/{modelId}/bpmn -> Get BPMN 2.0 xml
     */
    @RequestMapping(value="/rest/models/{processModelId}/bpmn20", method = RequestMethod.GET)
    public void getProcessModelBpmn20Xml(HttpServletResponse response, @PathVariable Long processModelId) throws IOException {  
		super.getProcessModelBpmn20Xml(response, processModelId);
    }
	
	 /**
     * GET /rest/models/{modelId}/history/{processModelHistoryId}/bpmn20 -> Get BPMN 2.0 xml
     */
    @RequestMapping(value="/rest/models/{processModelId}/history/{processModelHistoryId}/bpmn20", method = RequestMethod.GET)
    public void getHistoricProcessModelBpmn20Xml(HttpServletResponse response, @PathVariable Long processModelId, @PathVariable Long processModelHistoryId) throws IOException {
		super.getHistoricProcessModelBpmn20Xml(response, processModelId, processModelHistoryId);
    }
	
}
