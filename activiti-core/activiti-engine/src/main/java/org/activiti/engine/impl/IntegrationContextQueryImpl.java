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
package org.activiti.engine.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextEntity;
import org.activiti.engine.integration.IntegrationContextQuery;

public class IntegrationContextQueryImpl
    extends AbstractQuery<IntegrationContextQuery, IntegrationContextEntity>
    implements IntegrationContextQuery, Serializable {

    private static final long serialVersionUID = 1L;

    protected Date createdBefore;
    protected Date createdAfter;

    public IntegrationContextQueryImpl(CommandExecutor commandExecutor) {
        super(commandExecutor);
    }

    @Override
    public IntegrationContextQuery createdBefore(Date date) {
        if (date == null) {
            throw new ActivitiIllegalArgumentException("Provided date is null");
        }
        this.createdBefore = date;
        return this;
    }

    @Override
    public IntegrationContextQuery createdAfter(Date date) {
        if (date == null) {
            throw new ActivitiIllegalArgumentException("Provided date is null");
        }
        this.createdAfter = date;
        return this;
    }

    @Override
    public IntegrationContextQuery orderByCreatedDate() {
        return orderBy(IntegrationContextQueryProperty.CREATED_DATE);
    }

    @Override
    public long executeCount(CommandContext commandContext) {
        checkQueryOk();
        return commandContext
            .getProcessEngineConfiguration()
            .getIntegrationContextManager()
            .findCountByQueryCriteria(this);
    }

    @Override
    public List<IntegrationContextEntity> executeList(CommandContext commandContext, Page page) {
        checkQueryOk();
        return commandContext
            .getProcessEngineConfiguration()
            .getIntegrationContextManager()
            .findByQueryCriteria(this, page);
    }

    public Date getCreatedBefore() {
        return createdBefore;
    }

    public Date getCreatedAfter() {
        return createdAfter;
    }
}
