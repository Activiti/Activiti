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
package com.activiti.domain.editor;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.activiti.domain.common.IdBlockSize;
import com.activiti.domain.idm.User;

@MappedSuperclass
public class AbstractModel {
	
	public static final int MODEL_TYPE_BPMN = 0;
	public static final int MODEL_TYPE_FORM = 2;
	public static final int MODEL_TYPE_APP = 3;
	public static final int MODEL_TYPE_DECISION_TABLE = 4;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "modelIdGenerator")
	@TableGenerator(name = "modelIdGenerator", allocationSize = IdBlockSize.DEFAULT_ALLOCATION_SIZE)
	@Column(name="id")
	protected Long id;
	
	@Column(name="name")
	protected String name;

	@Column(name="description")
	protected String description;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="created")
	protected Date created;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="last_updated")
	protected Date lastUpdated;
	
	@ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="created_by")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private User createdBy;
	
	@ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="last_updated_by")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private User lastUpdatedBy;

	@Column(name="version")
	protected int version;
	
	@Column(name="model_editor_json")
	protected String modelEditorJson;
	
	@Column(name="model_comment")
    protected String comment;
	
	@Column(name="stencil_set_id")
	protected Long stencilSetId;
	
	@Column(name="reference_id")
    protected Long referenceId;
	
	@Column(name="model_type")
	protected Integer modelType;
	
	public AbstractModel() {
		this.created = new Date();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreated() {
		return created;
	}
	
	public void setCreated(Date created) {
	  	this.created = created;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public User getCreatedBy() {
        return createdBy;
    }
	
	public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
	
	public User getLastUpdatedBy() {
        return lastUpdatedBy;
    }
	
	public void setLastUpdatedBy(User lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getModelEditorJson() {
		return modelEditorJson;
	}

	public void setModelEditorJson(String modelEditorJson) {
		this.modelEditorJson = modelEditorJson;
	}
	
	public void setComment(String comment) {
        this.comment = comment;
    }
	
	public String getComment() {
        return comment;
    }

    public Long getStencilSetId() {
        return stencilSetId;
    }

    public void setStencilSetId(Long stencilSetId) {
        this.stencilSetId = stencilSetId;
    }

	public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public Integer getModelType() {
		return modelType;
	}

	public void setModelType(Integer modelType) {
		this.modelType = modelType;
	}
	
}
