/**
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.model.editor.decisiontable;

import java.util.List;
import java.util.Map;

/**
 * Created by yvoswillens on 14/08/15.
 */
public class DecisionTableExpressionRepresentation {

    public static final String VARIABLE_TYPE_VARIABLE = "variable";

    protected String id;
    protected String variableId;
    protected String variableType;
    protected String type;
    protected String label;
    protected List<Map<String,String>> entries;
    protected boolean newVariable;

    public boolean isNewVariable() {
        return newVariable;
    }

    public void setNewVariable(boolean newVariable) {
        this.newVariable = newVariable;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVariableId() {
        return variableId;
    }

    public void setVariableId(String variableId) {
        this.variableId = variableId;
    }

    public String getVariableType() {
        return variableType;
    }

    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Map<String, String>> getEntries() {
        return entries;
    }

    public void setEntries(List<Map<String, String>> entries) {
        this.entries = entries;
    }
}
