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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.activiti.domain.editor.ModelInformation;
import com.activiti.domain.editor.ModelRelation;
import com.activiti.repository.editor.ModelRelationRepository;
import com.activiti.repository.editor.ModelRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author jbarrez
 */
@Service
@Transactional
public class ModelRelationService {
	
	private static final Logger logger = LoggerFactory.getLogger(ModelRelationService.class);
	
	@Autowired
	private ModelRepository modelRepository;
	
	@Autowired
	private ModelRelationRepository modelRelationRepository;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	/**
	 * Returns the models which have a {@link ModelRelation} with the given model, 
	 * where the model references those directly or indirectly.
	 * 
	 * ie modelId here is the parent of the relation.
	 */
	public List<ModelInformation> findReferencedModels(Long modelId) {
		return convert(modelRelationRepository.findModelInformationByParentModelId(modelId));
	}
	
	/**
	 * Returns the models which have a {@link ModelRelation} with the given model,
	 * where the given model is the one that is referenced.
	 * 
	 * ie modelId is the child of the relation
	 */
	public List<ModelInformation> findParentModels(Long modelId) {
		return convert(modelRelationRepository.findModelInformationByChildModelId(modelId));
	}
	
	protected List<ModelInformation> convert(List<Object[]> rawResults) {
		List<ModelInformation> result = new ArrayList<ModelInformation>(rawResults.size());
		for (Object[] rawResult : rawResults) {
			result.add(new ModelInformation(((Number) rawResult[0]).longValue(), (String) rawResult[1], (Integer) rawResult[2]));
		}
		return result;
	}

}
