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
package com.activiti.service.runtime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.activiti.model.editor.form.FormFieldRepresentation;
import com.activiti.model.editor.form.FormFieldTypes;
import com.activiti.model.editor.form.FormValuePlaceholderRepresentation;
import com.activiti.model.idm.LightGroupRepresentation;
import com.activiti.model.idm.LightUserRepresentation;
import com.activiti.model.runtime.RelatedContentRepresentation;
import com.fasterxml.jackson.databind.util.ISO8601Utils;


/**
 * {@link FormValueSegment} that will resolves to the value of a single field value.
 * 
 * @author Frederik Heremans
 */
public class FieldValueFormValueSegment extends FormValueSegment {

    private static final DateFormat dateFormat = new SimpleDateFormat("MMMM dd yyyy");
    private String fieldId;
    
    public FieldValueFormValueSegment(String fieldId) {
        this.fieldId = fieldId;
    }
    
    public String getFieldId() {
        return fieldId;
    }
    
    @Override
    public String getStringValue(Map<String, Object> fieldValues, Map<String, String> fieldTypes, FormFieldRepresentation field) {
        Object rawValue = fieldValues.get(fieldId);
        String fieldType = fieldTypes.get(fieldId);
        if (rawValue != null) {
            if (rawValue instanceof List<?>) {
                List<?> list = (List<?>) rawValue;
                if (list.size() > 0) {
                    Object item = list.get(0);
                    if (item instanceof RelatedContentRepresentation) {
                        return registerPlaceHolder(rawValue, field, FormFieldTypes.UPLOAD);
                    }
                } else {
                    return "";
                }
            } else if (rawValue instanceof LightUserRepresentation) {
                LightUserRepresentation user = (LightUserRepresentation) rawValue;
                return StringUtils.join(Arrays.asList(user.getFirstName(), user.getLastName()), ' ');
                
            } else if (rawValue instanceof LightGroupRepresentation) {
                LightGroupRepresentation group = (LightGroupRepresentation) rawValue;
                return group.getName();
                
            } else if (rawValue instanceof Date) {
                return registerPlaceHolder(rawValue, field, FormFieldTypes.DATE);
                
            } else if (FormFieldTypes.DATE.equals(fieldType) && rawValue instanceof String) {
                Date parsedDate = ISO8601Utils.parse((String) rawValue);
                if (parsedDate != null) {
                    return dateFormat.format(parsedDate);
                }
            }
            return rawValue.toString();
        } else {
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    protected String registerPlaceHolder(Object value, FormFieldRepresentation field, String type) {
        List<FormValuePlaceholderRepresentation> placeHolders = (List<FormValuePlaceholderRepresentation>) field.getParam("placeholders");
        if (placeHolders == null) {
            placeHolders = new ArrayList<FormValuePlaceholderRepresentation>();
            if (field.getParams() == null) {
                field.setParams(new HashMap<String, Object>());
            }
            field.getParams().put("placeholders", placeHolders);
        }
        
        String placeholder = UUID.randomUUID().toString();
        placeHolders.add(new FormValuePlaceholderRepresentation(placeholder, value, type));
        return placeholder;
    }
}
