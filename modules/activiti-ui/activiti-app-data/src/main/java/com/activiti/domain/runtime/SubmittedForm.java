/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
