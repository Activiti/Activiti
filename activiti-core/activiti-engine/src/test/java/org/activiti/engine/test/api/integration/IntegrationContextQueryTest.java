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
package org.activiti.engine.test.api.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextEntity;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextEntityImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.integration.IntegrationContextQuery;

public class IntegrationContextQueryTest extends PluggableActivitiTestCase {

    private static final long ONE_HOUR_MS = 60L * 60L * 1000L;

    private CommandExecutor commandExecutor;

    private String contextIdEarliest;
    private String contextIdMiddle;
    private String contextIdLatest;
    private Date dateEarliest;
    private Date dateMiddle;
    private Date dateLatest;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        commandExecutor = processEngineConfiguration.getCommandExecutor();

        dateEarliest = new Date(0);
        dateMiddle = new Date(ONE_HOUR_MS);
        dateLatest = new Date(2 * ONE_HOUR_MS);

        contextIdEarliest = insertContext(dateEarliest);
        contextIdMiddle = insertContext(dateMiddle);
        contextIdLatest = insertContext(dateLatest);
    }

    @Override
    protected void tearDown() throws Exception {
        deleteContext(contextIdEarliest);
        deleteContext(contextIdMiddle);
        deleteContext(contextIdLatest);
        super.tearDown();
    }

    private String insertContext(Date createdDate) {
        return commandExecutor.execute(commandContext -> {
            var entity = new IntegrationContextEntityImpl();
            entity.setCreatedDate(createdDate);
            commandContext.getProcessEngineConfiguration().getIntegrationContextManager().insert(entity);
            return entity.getId();
        });
    }

    private void deleteContext(String id) {
        commandExecutor.execute(commandContext -> {
            var manager = commandContext.getProcessEngineConfiguration().getIntegrationContextManager();
            var entity = manager.findById(id);
            if (entity != null) {
                manager.delete(entity);
            }
            return null;
        });
    }

    public void testQueryCreatedBefore() {
        List<IntegrationContextEntity> result = createQuery().createdBefore(dateMiddle).list();

        assertThat(result).extracting(IntegrationContextEntity::getId).containsExactlyInAnyOrder(contextIdEarliest);
    }

    public void testQueryCreatedAfter() {
        List<IntegrationContextEntity> result = createQuery().createdAfter(dateMiddle).list();

        assertThat(result).extracting(IntegrationContextEntity::getId).containsExactlyInAnyOrder(contextIdLatest);
    }

    public void testCountCreatedBefore() {
        long count = createQuery().createdBefore(dateLatest).count();

        assertThat(count).isEqualTo(2L);
    }

    public void testCountCreatedAfter() {
        long count = createQuery().createdAfter(dateEarliest).count();

        assertThat(count).isEqualTo(2L);
    }

    public void testOrderByCreatedDateAscending() {
        List<IntegrationContextEntity> result = createQuery().orderByCreatedDate().asc().list();

        assertThat(result)
            .extracting(IntegrationContextEntity::getId)
            .containsExactly(contextIdEarliest, contextIdMiddle, contextIdLatest);
    }

    public void testOrderByCreatedDateDescending() {
        List<IntegrationContextEntity> result = createQuery().orderByCreatedDate().desc().list();

        assertThat(result)
            .extracting(IntegrationContextEntity::getId)
            .containsExactly(contextIdLatest, contextIdMiddle, contextIdEarliest);
    }

    private IntegrationContextQuery createQuery() {
        return processEngineConfiguration.getIntegrationContextService().createIntegrationContextQuery();
    }
}
