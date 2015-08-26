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
package com.activiti.model.editor;

import com.activiti.model.common.AbstractRepresentation;


public class AppDefinitionUpdateResultRepresentation extends AbstractRepresentation {
    
    public static final int CUSTOM_STENCIL_ITEM = 1;
    public static final int MODEL_VALIDATION_ERRORS = 2;

    protected AppDefinitionRepresentation appDefinition;
    protected String message;
    protected String messageKey;
    protected boolean error;
    protected int errorType;
    protected String errorDescription;
    
    public AppDefinitionRepresentation getAppDefinition() {
        return appDefinition;
    }
    public void setAppDefinition(AppDefinitionRepresentation appDefinition) {
        this.appDefinition = appDefinition;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessageKey() {
        return messageKey;
    }
    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }
    public boolean isError() {
        return error;
    }
    public void setError(boolean error) {
        this.error = error;
    }
    public int getErrorType() {
        return errorType;
    }
    public void setErrorType(int errorType) {
        this.errorType = errorType;
    }
    public String getErrorDescription() {
        return errorDescription;
    }
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
}
