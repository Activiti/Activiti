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

import java.io.InputStreamReader;

import javax.inject.Inject;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.activiti.domain.editor.AbstractModel;
import com.activiti.domain.editor.Model;
import com.activiti.domain.idm.User;
import com.activiti.model.editor.ModelRepresentation;
import com.activiti.repository.editor.ModelShareInfoRepository;
import com.activiti.security.SecurityUtils;
import com.activiti.service.editor.ModelInternalService;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.NonJsonResourceNotFoundException;
import com.activiti.util.XmlUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AbstractModelResource extends BaseModelResource {

	@Inject
	protected ModelInternalService modelService;
	
	@Inject
	protected ModelShareInfoRepository shareInfoRepository;
	
	protected BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
	
	protected BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
	
	protected ObjectMapper objectMapper = new ObjectMapper();
	
    public ModelRepresentation getModel(Long modelId, Boolean includePermissions) {    
        Model model = getModel(modelId, true, false);
        ModelRepresentation result = new ModelRepresentation(model);
        
        if (includePermissions == null || Boolean.TRUE.equals(includePermissions)) {
           populatePermissions(model, result);
        }
        return result;
    }
    
    public byte[] getModelThumbnail(Long modelId) {    
        Model model = modelRepository.findOne(modelId);
        
        if (model == null) {
        	throw new NonJsonResourceNotFoundException();
        }

        return model.getThumbnail();
    }
    
    public ModelRepresentation importNewVersion(Long modelId, MultipartFile file) {

        Model processModel = getModel(modelId, true, true);
        User currentUser = SecurityUtils.getCurrentUserObject();
        
        String fileName = file.getOriginalFilename();
        if (fileName != null && (fileName.endsWith(".bpmn") || fileName.endsWith(".bpmn20.xml"))) {
            try {
                XMLInputFactory xif = XmlUtil.createSafeXmlInputFactory();
                InputStreamReader xmlIn = new InputStreamReader(file.getInputStream(), "UTF-8");
                XMLStreamReader xtr = xif.createXMLStreamReader(xmlIn);
                BpmnModel bpmnModel = bpmnXMLConverter.convertToBpmnModel(xtr);
                if (CollectionUtils.isEmpty(bpmnModel.getProcesses())) {
                    throw new BadRequestException("No process found in definition " + fileName);
                }
                
                if (bpmnModel.getLocationMap().size() == 0) {
                    throw new BadRequestException("No required BPMN DI information found in definition " + fileName);
                }
                
                ObjectNode modelNode = bpmnJsonConverter.convertToJson(bpmnModel);
                
                AbstractModel savedModel = modelService.saveModel(modelId, processModel.getName(), processModel.getDescription(), 
                        modelNode.toString(), true, "Version import via REST service", currentUser);
                return new ModelRepresentation(savedModel);
             
            } catch (BadRequestException e) {
                throw e;
                
            } catch (Exception e) {
                throw new BadRequestException("Import failed for " + fileName + ", error message " + e.getMessage());
            }
        } else {
            throw new BadRequestException("Invalid file name, only .bpmn and .bpmn20.xml files are supported not " + fileName);
        }
    }

}
