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
package org.activiti.engine.impl.cmd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.runtime.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClaimNextCandidateTaskCmdTest {

    @Mock
    private CommandContext commandContext;

    @Mock
    private ProcessEngineConfigurationImpl processEngineConfiguration;

    @Mock
    private Clock clock;

    @Mock
    private DbSqlSession dbSqlSession;

    private Date currentTime;

    @BeforeEach
    void setUp() {
        currentTime = new Date();
        lenient().when(commandContext.getProcessEngineConfiguration()).thenReturn(processEngineConfiguration);
        lenient().when(processEngineConfiguration.getClock()).thenReturn(clock);
        lenient().when(clock.getCurrentTime()).thenReturn(currentTime);
        lenient().when(commandContext.getDbSqlSession()).thenReturn(dbSqlSession);
    }

    @Test
    void should_throwException_whenUserIdIsNull() {
        ClaimNextCandidateTaskCmd cmd = new ClaimNextCandidateTaskCmd(null, List.of("team"));

        assertThatThrownBy(() -> cmd.execute(commandContext))
            .isInstanceOf(ActivitiIllegalArgumentException.class)
            .hasMessage("User id is null or empty");
    }

    @Test
    void should_throwException_whenUserIdIsEmpty() {
        ClaimNextCandidateTaskCmd cmd = new ClaimNextCandidateTaskCmd("", List.of("team"));

        assertThatThrownBy(() -> cmd.execute(commandContext))
            .isInstanceOf(ActivitiIllegalArgumentException.class)
            .hasMessage("User id is null or empty");
    }

    @Test
    void should_returnTrue_whenCandidateTaskIsClaimed() {
        ClaimNextCandidateTaskCmd cmd = new ClaimNextCandidateTaskCmd("john", List.of("activitiTeam"));
        when(dbSqlSession.update(eq("claimNextUnassignedCandidateTask"), anyMap())).thenReturn(1);

        boolean claimed = cmd.execute(commandContext);

        assertThat(claimed).isTrue();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(dbSqlSession).update(eq("claimNextUnassignedCandidateTask"), paramsCaptor.capture());

        Map<String, Object> params = paramsCaptor.getValue();
        assertThat(params.get("userId")).isEqualTo("john");
        assertThat(params.get("userGroups")).isEqualTo(List.of("activitiTeam"));
        assertThat(params.get("claimTime")).isEqualTo(currentTime);
    }

    @Test
    void should_returnFalse_whenNoCandidateTaskIsClaimed() {
        ClaimNextCandidateTaskCmd cmd = new ClaimNextCandidateTaskCmd("john", List.of("activitiTeam"));
        when(dbSqlSession.update(eq("claimNextUnassignedCandidateTask"), anyMap())).thenReturn(0);

        boolean claimed = cmd.execute(commandContext);

        assertThat(claimed).isFalse();
    }

    @Test
    void should_useEmptyGroups_whenUserGroupsAreNull() {
        ClaimNextCandidateTaskCmd cmd = new ClaimNextCandidateTaskCmd("john", null);
        when(dbSqlSession.update(eq("claimNextUnassignedCandidateTask"), anyMap())).thenReturn(0);

        cmd.execute(commandContext);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(dbSqlSession).update(eq("claimNextUnassignedCandidateTask"), paramsCaptor.capture());

        assertThat(paramsCaptor.getValue().get("userGroups")).isEqualTo(Collections.emptyList());
    }
}
