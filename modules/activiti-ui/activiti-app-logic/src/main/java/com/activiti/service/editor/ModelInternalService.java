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
package com.activiti.service.editor;

import org.activiti.bpmn.model.BpmnModel;

import com.activiti.domain.editor.AbstractModel;
import com.activiti.domain.editor.Model;
import com.activiti.domain.editor.ModelHistory;
import com.activiti.domain.idm.User;
import com.activiti.model.editor.ModelRepresentation;
import com.activiti.model.editor.ReviveModelResultRepresentation;
import com.activiti.service.api.ModelService;

public interface ModelInternalService extends ModelService {

	byte[] getBpmnXML(AbstractModel model, User user);

	byte[] getBpmnXML(BpmnModel bpmnModel);

	BpmnModel getBpmnModel(AbstractModel model, User user, boolean refreshReferences);

	Model createModel(ModelRepresentation model, String editorJson, User createdBy);

	Model saveModel(Model modelObject);
	
	Model saveModel(Model modelObject, String editorJson, byte[] imageBytes, boolean newVersion, String newVersionComment, User updatedBy);

	Model saveModel(long modelId, String name, String description, String editorJson, boolean newVersion, String newVersionComment, User updatedBy);

	Model createNewModelVersion(Model modelObject, String comment, User updatedBy);
	
	ModelHistory createNewModelVersionAndReturnModelHistory(Model modelObject, String comment, User updatedBy);

	void deleteModel(long modelId, boolean cascadeHistory, boolean deleteRuntimeApp);

	ReviveModelResultRepresentation reviveProcessModelHistory(ModelHistory modelHistory, User user, String newVersionComment);

}