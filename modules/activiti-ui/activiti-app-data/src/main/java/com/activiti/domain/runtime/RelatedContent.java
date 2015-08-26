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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;

import com.activiti.domain.common.IdBlockSize;
import com.activiti.domain.idm.User;


/**
 * @author Frederik Heremans
 */
@Entity
@Table(name = "RELATED_CONTENT")
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private User createdBy;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lock_owner")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private User lockOwner;

    @Column(name="checked_out")
    private boolean checkedOut;

    @Column(name="checked_out_to_local")
    private boolean checkedOutToLocal;

    @Column(name="checkout_date")
    private Date checkoutDate;
    
    @Column(name="store_id")
    private String contentStoreId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkout_owner")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private User checkoutOwner;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified")
    protected Date lastModified;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "last_modified_by")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private User lastModifiedBy;

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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
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

    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(User lastModifiedBy) {
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
    
    public User getLockOwner() {
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
    
    public User getCheckoutOwner() {
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
    
    public void setLockOwner(User lockOwner) {
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
    
    public void setCheckoutOwner(User checkoutOwner) {
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
