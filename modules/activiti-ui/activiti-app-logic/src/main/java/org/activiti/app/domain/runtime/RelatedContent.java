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
package org.activiti.app.domain.runtime;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.activiti.app.domain.common.IdBlockSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;


/**
 * @author Frederik Heremans
 */
@Entity
@Table(name = "ACT_WO_RELATED_CONTENT")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class RelatedContent {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator="relatedContentGenerator")
    @TableGenerator(name = "relatedContentGenerator", allocationSize = IdBlockSize.DEFAULT_ALLOCATION_SIZE)
    @Column(name = "id")
    protected Long id;

    @Length(max = 255)
    @Column(name = "name")
    protected String name;
    
    @Length(max = 255)
    @Column(name = "mime_type")
    protected String mimeType;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created")
    protected Date created;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "task_id")
    private String taskId;

    @Column(name = "process_id")
    private String processInstanceId;
    
    @Column(name = "content_source")
    private String source;
    
    @Column(name = "source_id")
    private String sourceId;
    
    @Column(name = "content_available")
    private boolean contentAvailable;
    
    @Column(name = "locked")
    private boolean locked;

    @Column(name="lock_date")
    private Date lockDate;

    @Column(name="lock_exp_date")
    private Date lockExpirationDate;

    @Column(name = "lock_owner")
    private String lockOwner;

    @Column(name="checked_out")
    private boolean checkedOut;

    @Column(name="checked_out_to_local")
    private boolean checkedOutToLocal;

    @Column(name="checkout_date")
    private Date checkoutDate;
    
    @Column(name="store_id")
    private String contentStoreId;

    @Column(name = "checkout_owner")
    private String checkoutOwner;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified")
    protected Date lastModified;

    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @Column(name = "field")
    private String field;
    
    @Column(name = "related_content")
    private boolean relatedContent = false;
    
    @Column(name = "link")
    private boolean link = false;
    
    @Column(name = "link_url")
    private String linkUrl;
    
    @Column(name="content_size")
    private Long contentSize;

    
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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    // new stuff
    
    public Date getLastModified() {
    	return this.lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
    
    public boolean isLocked() {
    	return locked;
    }
    
    public Date getLockDate() {
    	return lockDate;
    }
    
    public Date getLockExpiration() {
    	return lockExpirationDate;
    }
    
    public String getLockOwner() {
    	return lockOwner;
    }
    
    public boolean isCheckedOut() {
    	return checkedOut;
    }
    
    public boolean isCheckedOutToLocal() {
    	return checkedOutToLocal;
    }
    
    public Date getCheckoutDate() {
    	return checkoutDate;
    }
    
    public String getCheckoutOwner() {
    	return checkoutOwner;
    }
    
    public String getVersionLabel() {
    	return "1.0";
    }
    
    public void setLockDate(Date lockDate) {
        this.lockDate = lockDate;
    }
    
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    
    public void setLockExpirationDate(Date lockExpirationDate) {
        this.lockExpirationDate = lockExpirationDate;
    }
    
    public void setLockOwner(String lockOwner) {
        this.lockOwner = lockOwner;
    }
    
    public void setCheckedOut(boolean checkedOut) {
        this.checkedOut = checkedOut;
    }
    
    public void setCheckedOutToLocal(boolean checkedOutToLocal) {
        this.checkedOutToLocal = checkedOutToLocal;
    }
    
    public void setCheckoutDate(Date checkoutDate) {
        this.checkoutDate = checkoutDate;
    }
    
    public void setCheckoutOwner(String checkoutOwner) {
        this.checkoutOwner = checkoutOwner;
    }
    
    public void setContentAvailable(boolean contentAvailable) {
        this.contentAvailable = contentAvailable;
    }
    
    public boolean isContentAvailable() {
        return contentAvailable;
    }
    
    public String getContentStoreId() {
        return contentStoreId;
    }
    
    public void setContentStoreId(String contentStoreId) {
        this.contentStoreId = contentStoreId;
    }

    public void setField(String field) {
        this.field = field;
    }
    
    public String getField() {
        return field;
    }
    
    public void setRelatedContent(boolean relatedContent) {
        this.relatedContent = relatedContent;
    }
    
    public boolean isRelatedContent() {
        return relatedContent;
    }
    
    public boolean isLink() {
        return link;
    }
    
    public void setLink(boolean link) {
        this.link = link;
    }
    
    public String getLinkUrl() {
        return linkUrl;
    }
    
    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }
    
    public Long getContentSize() {
        return contentSize;
    }
    
    public void setContentSize(Long contentSize) {
        this.contentSize = contentSize;
    }
}
