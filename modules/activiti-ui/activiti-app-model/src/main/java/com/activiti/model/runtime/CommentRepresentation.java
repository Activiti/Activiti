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
package com.activiti.model.runtime;

import com.activiti.domain.runtime.Comment;
import com.activiti.model.common.AbstractRepresentation;
import com.activiti.model.idm.LightUserRepresentation;

import java.util.Date;

public class CommentRepresentation extends AbstractRepresentation {

    private Long id;
    private String message;
    private Date created;
    private LightUserRepresentation createdBy;

    public CommentRepresentation(Comment comment) {
        this.id = comment.getId();
        this.message = comment.getMessage();
        this.created = comment.getCreated();

        if (comment.getCreatedBy() != null) {
            this.createdBy = new LightUserRepresentation(comment.getCreatedBy());
        }
    }

    public CommentRepresentation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public LightUserRepresentation getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(LightUserRepresentation createdBy) {
        this.createdBy = createdBy;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
