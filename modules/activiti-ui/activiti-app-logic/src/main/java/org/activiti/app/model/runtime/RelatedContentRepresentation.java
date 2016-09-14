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
package org.activiti.app.model.runtime;

import java.util.Date;

import org.activiti.app.domain.runtime.RelatedContent;
import org.activiti.app.model.common.AbstractRepresentation;
import org.activiti.app.model.component.SimpleContentTypeMapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author Frederik Heremans
 */
public class RelatedContentRepresentation extends AbstractRepresentation {

  protected Long id;

  protected String name;

  protected Date created;

  protected String createdBy;

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
    this.createdBy = content.getCreatedBy();
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

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
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
