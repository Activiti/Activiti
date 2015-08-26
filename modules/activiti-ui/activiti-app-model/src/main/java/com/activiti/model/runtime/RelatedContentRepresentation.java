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

import com.activiti.domain.runtime.RelatedContent;
import com.activiti.model.common.AbstractRepresentation;
import com.activiti.model.component.SimpleContentTypeMapper;
import com.activiti.model.idm.LightUserRepresentation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Date;

/**
 * @author Frederik Heremans
 */
public class RelatedContentRepresentation extends AbstractRepresentation {

    protected Long id;

    protected String name;

    protected Date created;

    protected LightUserRepresentation createdBy;

    protected boolean contentAvailable;

    protected boolean link;

    protected String source;

    protected String sourceId;

    protected String mimeType;

    protected String simpleType;

    protected String linkUrl;

    public String previewStatus = "queued";

    public String thumbnailStatus = "queued";

    public RelatedContentRepresentation() {

    }

    public RelatedContentRepresentation(RelatedContent content, SimpleContentTypeMapper mapper) {
        this.id = content.getId();
        this.name = content.getName();
        this.created = content.getCreated();
        if (content.getCreatedBy() != null) {
            this.createdBy = new LightUserRepresentation(content.getCreatedBy());
        }
        this.contentAvailable = content.isContentAvailable();
        this.mimeType = content.getMimeType();
        this.link = content.isLink();
        this.linkUrl = content.getLinkUrl();
        this.source = content.getSource();
        this.sourceId = content.getSourceId();

        if (mapper != null) {
            this.simpleType = mapper.getSimpleType(content);
        }
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

    public boolean isContentAvailable() {
        return contentAvailable;
    }

    public void setContentAvailable(boolean contentAvailable) {
        this.contentAvailable = contentAvailable;
    }

    @JsonInclude(Include.NON_NULL)
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @JsonInclude(Include.NON_NULL)
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

    @JsonInclude(Include.NON_NULL)
    public String getSimpleType() {
        return simpleType;
    }

    public void setSimpleType(String simpleType) {
        this.simpleType = simpleType;
    }

    public void setLink(boolean link) {
        this.link = link;
    }

    public boolean isLink() {
        return link;
    }

    @JsonInclude(Include.NON_NULL)
    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getThumbnailStatus() {
        return thumbnailStatus;
    }

    public void setThumbnailStatus(String thumbnailStatus) {
        this.thumbnailStatus = thumbnailStatus;
    }

    public String getPreviewStatus() {
        return previewStatus;
    }

    public void setPreviewStatus(String previewStatus) {
        this.previewStatus = previewStatus;
    }
}
