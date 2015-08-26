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
package com.activiti.model.editor.form;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * @author Joram Barrez
 */
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "fieldType", defaultImpl = FormFieldRepresentation.class)
@JsonSubTypes({ @Type(ContainerRepresentation.class) })
public class FormFieldRepresentation {

    protected String id;
    protected String name;
    protected String type;
    protected Object value;
    protected boolean required;
    protected boolean readOnly;
    protected boolean overrideId;
    protected String placeholder;
    protected String optionType;
    protected Boolean hasEmptyValue; // Needs to be Boolean with a big B for backwards compatibility
    protected List<OptionRepresentation> options;
    protected String restUrl;
    protected String restIdProperty;
    protected String restLabelProperty;
    protected Map<String, Object> params;

    protected LayoutRepresentation layout;
    protected int sizeX;
    protected int sizeY;
    protected int row;
    protected int col;

    protected ConditionRepresentation visibilityCondition;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isOverrideId() {
        return overrideId;
    }

    public void setOverrideId(boolean overrideId) {
        this.overrideId = overrideId;
    }

    public String getOptionType() {
        return optionType;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }
    
    public Boolean getHasEmptyValue() {
		return hasEmptyValue;
	}

	public void setHasEmptyValue(Boolean hasEmptyValue) {
		this.hasEmptyValue = hasEmptyValue;
	}

	public List<OptionRepresentation> getOptions() {
        return options;
    }

    public void setOptions(List<OptionRepresentation> options) {
        this.options = options;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getRestUrl() {
        return restUrl;
    }

    public void setRestUrl(String restUrl) {
        this.restUrl = restUrl;
    }

    public String getRestIdProperty() {
        return restIdProperty;
    }

    public void setRestIdProperty(String restIdProperty) {
        this.restIdProperty = restIdProperty;
    }

    public String getRestLabelProperty() {
        return restLabelProperty;
    }

    public void setRestLabelProperty(String restLabelProperty) {
        this.restLabelProperty = restLabelProperty;
    }

    public LayoutRepresentation getLayout() {
        return layout;
    }

    public void setLayout(LayoutRepresentation layout) {
        this.layout = layout;
    }

    public int getSizeX() {
        return sizeX;
    }

    public void setSizeX(int sizeX) {
        this.sizeX = sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public void setSizeY(int sizeY) {
        this.sizeY = sizeY;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public ConditionRepresentation getVisibilityCondition() {
        return visibilityCondition;
    }

    public void setVisibilityCondition(
            ConditionRepresentation visibilityCondition) {
        this.visibilityCondition = visibilityCondition;
    }

    @JsonInclude(Include.NON_EMPTY)
    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    @JsonIgnore
    public Object getParam(String name) {
        if (params != null) {
            return params.get(name);
        }
        return null;
    }
    
    @JsonIgnore
    public boolean isDisplayOnly(){
    	return FormFieldTypes.DISPLAY_ONLY_TYPES.contains(type);
    }

}
