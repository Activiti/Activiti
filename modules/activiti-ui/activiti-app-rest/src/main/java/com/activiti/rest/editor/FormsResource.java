/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.activiti.rest.editor;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.editor.AbstractModel;
import com.activiti.domain.editor.Model;
import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.editor.EditorValuesRepresentation;
import com.activiti.model.editor.FormFieldSummaryRepresentation;
import com.activiti.model.editor.FormFieldValuesRepresentation;
import com.activiti.model.editor.OutcomeValuesRepresentation;
import com.activiti.model.editor.form.ContainerRepresentation;
import com.activiti.model.editor.form.FormDefinitionRepresentation;
import com.activiti.model.editor.form.FormFieldRepresentation;
import com.activiti.model.editor.form.FormRepresentation;
import com.activiti.security.SecurityUtils;
import com.activiti.service.editor.ModelInternalService;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.InternalServerErrorException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tijs Rademakers
 */
@RestController
@RequestMapping("/rest/form-models")
public class FormsResource extends BaseModelResource {
    
    private static final Logger logger = LoggerFactory.getLogger(FormsResource.class);
    
    private static final int MIN_FILTER_LENGTH = 2;
    private static final int PAGE_SIZE = 1000;
	
    @Inject
    protected ModelInternalService modelService;
    
	protected ObjectMapper objectMapper = new ObjectMapper();
	
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public ResultListDataRepresentation getForms(@RequestParam(required=true) Long referenceId, HttpServletRequest request) {
	    
	    // need to parse the filterText parameter ourselves, due to encoding issues with the default parsing.
        String filter = null;
        List<NameValuePair> params = URLEncodedUtils.parse(request.getQueryString(), Charset.forName("UTF-8"));
        if (params != null) {
            for (NameValuePair nameValuePair : params) {
                if ("filter".equalsIgnoreCase(nameValuePair.getName())) {
                    filter = nameValuePair.getValue();
                }
            }
        }
	    String validFilter = makeValidFilterText(filter);
	    
	    List<Model> models = null;
        if (validFilter != null) {
            models = modelRepository.findModelsByModelTypeAndReferenceId(AbstractModel.MODEL_TYPE_FORM, validFilter, referenceId);
            List<Model> createdByModels = modelRepository.findModelsCreatedBy(SecurityUtils.getCurrentUserId(), AbstractModel.MODEL_TYPE_FORM, validFilter, new Sort(Direction.ASC, "name"));
            if (CollectionUtils.isNotEmpty(createdByModels)) {
                models.addAll(createdByModels);
            }
            
        } else {
            models = modelRepository.findModelsByModelTypeAndReferenceId(AbstractModel.MODEL_TYPE_FORM, referenceId);
            List<Model> createdByModels = modelRepository.findModelsCreatedBy(SecurityUtils.getCurrentUserId(), AbstractModel.MODEL_TYPE_FORM, new Sort(Direction.ASC, "name"));
            if (CollectionUtils.isNotEmpty(createdByModels)) {
                models.addAll(createdByModels);
            }
        }
        
        List<FormRepresentation> reps = new ArrayList<FormRepresentation>();

        for (Model model : models) {
            reps.add(new FormRepresentation(model));
        }
        
        Collections.sort(reps, new NameComparator());

        ResultListDataRepresentation result = new ResultListDataRepresentation(reps);
        result.setTotal(models.size());
        return result;
	}
	
    @RequestMapping(value = "/editor-values", method = RequestMethod.GET, produces = "application/json")
    public EditorValuesRepresentation getEditorValues(HttpServletRequest request) {
        
	    String[] formIds = request.getParameterValues("formId");
        if (formIds == null || formIds.length == 0) {
            throw new BadRequestException("No formIds provided in the request");
        }
        
        EditorValuesRepresentation result = new EditorValuesRepresentation();
        
        for (String formId : formIds) {
            Model formModel = getModel(Long.valueOf(formId), true, false);
            FormDefinitionRepresentation formDefinitionRepresentation = null;
            try {
                formDefinitionRepresentation = objectMapper.readValue(formModel.getModelEditorJson(), FormDefinitionRepresentation.class);
            } catch (Exception e) {
                logger.error("Error deserializing form", e);
                throw new InternalServerErrorException("Could not deserialize form definition");
            }

            List <FormFieldRepresentation> allFields = formDefinitionRepresentation.listAllFields();
            if (CollectionUtils.isNotEmpty(allFields) ||
                    CollectionUtils.isNotEmpty(formDefinitionRepresentation.getOutcomes())) {
                
                FormFieldValuesRepresentation formfieldValues = new FormFieldValuesRepresentation();
                for (FormFieldRepresentation field : allFields) {
                    if (!(field instanceof ContainerRepresentation)) {
                        FormFieldSummaryRepresentation summaryField = new FormFieldSummaryRepresentation();
                        summaryField.setId(field.getId());
                        summaryField.setName(field.getName());
                        summaryField.setType(field.getType());
                        formfieldValues.getFields().add(summaryField);
                    }
                }
                
                formfieldValues.setFormId(formModel.getId());
                formfieldValues.setFormName(formModel.getName());
                result.getFormFieldValues().add(formfieldValues);
                
                OutcomeValuesRepresentation outcomeValues = new OutcomeValuesRepresentation();
                outcomeValues.setFormId(formModel.getId());
                outcomeValues.setFormName(formModel.getName());
                outcomeValues.setOutcomes(formDefinitionRepresentation.getOutcomes());
                result.getOutcomeValues().add(outcomeValues);
            }
        }
        
        return result;
    }
	
	protected String makeValidFilterText(String filterText) {
        String validFilter = null;
        
        if (filterText != null) {
            String trimmed = StringUtils.trim(filterText);
            if (trimmed.length() >= MIN_FILTER_LENGTH) {
                validFilter = "%" + trimmed.toLowerCase() + "%";
            }
        }
        return validFilter;
    }
	
	class NameComparator implements Comparator<FormRepresentation> {
	    @Override
	    public int compare(FormRepresentation o1, FormRepresentation o2) {
	        return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
	    }
	}
}
