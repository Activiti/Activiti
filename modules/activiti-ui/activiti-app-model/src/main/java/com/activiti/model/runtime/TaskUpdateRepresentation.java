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

import com.activiti.model.common.AbstractRepresentation;

import java.util.Date;

public class TaskUpdateRepresentation extends AbstractRepresentation {

    private String name;
    private String description;
    private Date dueDate;
    
    private boolean nameSet;
    private boolean descriptionSet;
    private boolean dueDateSet;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.nameSet = true;
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.descriptionSet = true;
        this.description = description;
    }
    public Date getDueDate() {
        return dueDate;
    }
    public void setDueDate(Date dueDate) {
        this.dueDateSet = true;
        this.dueDate = dueDate;
    }
    public boolean isNameSet() {
        return nameSet;
    }
    public boolean isDescriptionSet() {
        return descriptionSet;
    }
    public boolean isDueDateSet() {
        return dueDateSet;
    }
}
