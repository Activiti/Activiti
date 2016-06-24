/**
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.model.editor;

import com.activiti.model.common.AbstractRepresentation;
import com.activiti.model.editor.decisiontable.DecisionTableRepresentation;


public class DecisionTableSaveRepresentation extends AbstractRepresentation {

    protected boolean reusable;
    protected boolean newVersion;
    protected String comment;
    protected String decisionTableImageBase64;
    protected DecisionTableRepresentation decisionTableRepresentation;

    public boolean isReusable() {
        return reusable;
    }
    public void setReusable(boolean reusable) {
        this.reusable = reusable;
    }
    public boolean isNewVersion() {
        return newVersion;
    }
    public void setNewVersion(boolean newVersion) {
        this.newVersion = newVersion;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getDecisionTableImageBase64() {
		return decisionTableImageBase64;
	}
	public void setDecisionTableImageBase64(String decisionTableImageBase64) {
		this.decisionTableImageBase64 = decisionTableImageBase64;
	}
	public DecisionTableRepresentation getDecisionTableRepresentation() {
        return decisionTableRepresentation;
    }
    public void setDecisionTableRepresentation(DecisionTableRepresentation decisionTableRepresentation) {
        this.decisionTableRepresentation = decisionTableRepresentation;
    }
}
