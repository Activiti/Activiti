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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.editor.Model;
import com.activiti.domain.editor.ModelHistory;
import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.editor.ModelRepresentation;
import com.activiti.service.editor.ModelInternalService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class AbstractModelHistoryResource extends BaseModelResource {

	@Inject
	protected ModelInternalService modelService;
	
	protected ObjectMapper objectMapper = new ObjectMapper();
	
    public ResultListDataRepresentation getModelHistoryCollection(Long modelId, Boolean includeLatestVersion) {

        Model model = getModel(modelId, true, false);
        List<ModelHistory> history = historyRepository.findByModelIdAndRemovalDateIsNullOrderByVersionDesc(model.getId());
        ResultListDataRepresentation result = new ResultListDataRepresentation();
        
        List<ModelRepresentation> representations = new ArrayList<ModelRepresentation>();
        
        // Also include the latest version of the model
        if (Boolean.TRUE.equals(includeLatestVersion)) {
            representations.add(new ModelRepresentation(model));
        }
        if (history.size() > 0) {
            for (ModelHistory modelHistory : history) {
                representations.add(new ModelRepresentation(modelHistory));
            }
            result.setData(representations);
        }
        
        // Set size and total
        result.setSize(representations.size());
        result.setTotal(representations.size());
        result.setStart(0);
        return result;
    }
    
    public ModelRepresentation getProcessModelHistory(Long modelId, Long modelHistoryId) {
        // Check if the user has read-rights on the process-model in order to fetch history
        ModelHistory modelHistory = getModelHistory(modelId, modelHistoryId, true, false);
        return new ModelRepresentation(modelHistory);
    }
    
}
