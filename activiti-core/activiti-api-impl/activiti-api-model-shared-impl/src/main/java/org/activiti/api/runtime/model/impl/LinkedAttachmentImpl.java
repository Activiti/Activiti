/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.api.runtime.model.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;
import org.activiti.api.model.shared.event.LinkedAttachment;

public class LinkedAttachmentImpl implements LinkedAttachment {

    private String type;
    private String link;

    @JsonCreator
    public LinkedAttachmentImpl() {}

    public LinkedAttachmentImpl(String type, String link) {
        this.type = type;
        this.link = link;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return "LinkedAttachmentImpl [type=" + type + ", link=" + link + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, link);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LinkedAttachmentImpl other = (LinkedAttachmentImpl) obj;
        return Objects.equals(type, other.type) && Objects.equals(link, other.link);
    }
}
