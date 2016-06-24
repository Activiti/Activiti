/**
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.model.editor.decisiontable;

/**
 * @author Bassam Al-Sarori
 */
public class DecisionTableDefinitionModelRepresentation {

    protected DecisionTableDefinitionRepresentation decisionTableDefinition;
    protected Long referenceId;
    protected String description;
    protected String editorJson;

    public DecisionTableDefinitionRepresentation getDecisionTableDefinition() {
        return decisionTableDefinition;
    }
    
    public void setDecisionTableDefinition(DecisionTableDefinitionRepresentation decisionTableDefinition) {
        this.decisionTableDefinition = decisionTableDefinition;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEditorJson() {
        return editorJson;
    }

    public void setEditorJson(String editorJson) {
        this.editorJson = editorJson;
    }
}
