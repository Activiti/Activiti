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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.integration.IntegrationContextQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IntegrationContextQueryImplTest {

    @Mock
    private CommandExecutor commandExecutor;

    private IntegrationContextQueryImpl query() {
        return new IntegrationContextQueryImpl(commandExecutor);
    }

    @Test
    void should_setCreatedBeforeDate_when_createdBeforeIsCalled() {
        var date = new Date();

        var result = query().createdBefore(date);

        assertThat(((IntegrationContextQueryImpl) result).getCreatedBefore()).isEqualTo(date);
    }

    @Test
    void should_setCreatedAfterDate_when_createdAfterIsCalled() {
        var date = new Date();

        var result = query().createdAfter(date);

        assertThat(((IntegrationContextQueryImpl) result).getCreatedAfter()).isEqualTo(date);
    }

    @Test
    void should_throwException_when_createdBeforeIsNull() {
        var query = query();
        assertThatThrownBy(() -> query.createdBefore(null)).isInstanceOf(ActivitiIllegalArgumentException.class);
    }

    @Test
    void should_throwException_when_createdAfterIsNull() {
        var query = query();
        assertThatThrownBy(() -> query.createdAfter(null)).isInstanceOf(ActivitiIllegalArgumentException.class);
    }

    @Test
    void should_returnSelf_when_createdBeforeIsCalled() {
        var q = query();

        var result = q.createdBefore(new Date());

        assertThat(result).isSameAs(q);
    }

    @Test
    void should_returnSelf_when_createdAfterIsCalled() {
        var q = query();

        var result = q.createdAfter(new Date());

        assertThat(result).isSameAs(q);
    }

    @Test
    void should_setOrderProperty_when_orderByCreatedDateIsCalled() {
        var q = query();

        IntegrationContextQuery result = q.orderByCreatedDate();

        assertThat(result).isSameAs(q);
        assertThat(q.orderProperty).isEqualTo(IntegrationContextQueryProperty.CREATED_DATE);
    }
}
