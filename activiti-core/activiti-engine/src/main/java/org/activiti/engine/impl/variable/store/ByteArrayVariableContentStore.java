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
package org.activiti.engine.impl.variable.store;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ByteArrayEntity;
import org.activiti.engine.impl.persistence.entity.ByteArrayEntityManager;

public class ByteArrayVariableContentStore implements VariableContentStore {

    public static final String STORE_NAME = "bytearray";

    @Override
    public String getStoreName() {
        return STORE_NAME;
    }

    @Override
    public String store(String variableName, String processInstanceId, byte[] bytes) {
        ByteArrayEntityManager mgr = Context.getCommandContext().getByteArrayEntityManager();
        ByteArrayEntity entity = mgr.create();
        entity.setName("var-" + variableName);
        entity.setBytes(bytes);
        mgr.insert(entity);
        return entity.getId();
    }

    @Override
    public byte[] load(String contentId) {
        ByteArrayEntityManager mgr = Context.getCommandContext().getByteArrayEntityManager();
        ByteArrayEntity entity = mgr.findById(contentId);
        return entity != null ? entity.getBytes() : null;
    }

    @Override
    public void delete(String contentId) {
        if (contentId != null) {
            Context.getCommandContext().getByteArrayEntityManager().deleteByteArrayById(contentId);
        }
    }
}
