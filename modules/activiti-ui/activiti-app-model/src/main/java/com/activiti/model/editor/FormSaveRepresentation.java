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
import com.activiti.model.editor.form.FormRepresentation;


public class FormSaveRepresentation extends AbstractRepresentation {

    protected boolean reusable;
    protected boolean newVersion;
    protected String comment;
    protected String formImageBase64;
    protected FormRepresentation formRepresentation;
    
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
    public String getFormImageBase64() {
		return formImageBase64;
	}
	public void setFormImageBase64(String formImageBase64) {
		this.formImageBase64 = formImageBase64;
	}
	public FormRepresentation getFormRepresentation() {
        return formRepresentation;
    }
    public void setFormRepresentation(FormRepresentation formRepresentation) {
        this.formRepresentation = formRepresentation;
    }
}
