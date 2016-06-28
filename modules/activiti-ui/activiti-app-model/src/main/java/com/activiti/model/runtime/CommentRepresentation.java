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
