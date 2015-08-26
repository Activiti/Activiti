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
package com.activiti.domain.runtime;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.activiti.domain.common.IdBlockSize;
import com.activiti.domain.idm.User;

/**
 * @author Tijs Rademakers
 */
@Entity
@Table(name = "SUBMITTED_FORM")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SubmittedForm {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "submittedFormIdGenerator")
	@TableGenerator(name = "submittedFormIdGenerator", allocationSize = IdBlockSize.DEFAULT_ALLOCATION_SIZE)
    @Column(name="id")
	private Long id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="form_id")
	private Form form;
	
	@Column(name="task_id")
	private String taskId;
	
	@Column(name="process_id")
    private String processId;
	
	@Column(name="submitted", nullable=false)
	private Date submitted;
	
	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="submitted_by")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	private User submittedBy;
	
	@Column(name="fields_value_definition")
	private String fieldsValueDefinition;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public Date getSubmitted() {
        return submitted;
    }

    public void setSubmitted(Date submitted) {
        this.submitted = submitted;
    }

    public User getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(User submittedBy) {
        this.submittedBy = submittedBy;
    }

    public String getFieldsValueDefinition() {
        return fieldsValueDefinition;
    }

    public void setFieldsValueDefinition(String fieldsValueDefinition) {
        this.fieldsValueDefinition = fieldsValueDefinition;
    }
}
