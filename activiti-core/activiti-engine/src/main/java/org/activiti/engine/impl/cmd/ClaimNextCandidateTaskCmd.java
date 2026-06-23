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

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

/**
 * Finds and claims the next candidate task for a user.
 */
public class ClaimNextCandidateTaskCmd implements Command<Boolean>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String userId;
    private final List<String> userGroups;

    public ClaimNextCandidateTaskCmd(String userId, List<String> userGroups) {
        this.userId = userId;
        this.userGroups = userGroups == null ? Collections.emptyList() : userGroups;
    }

    @Override
    public Boolean execute(CommandContext commandContext) {
        if (userId == null || userId.isEmpty()) {
            throw new ActivitiIllegalArgumentException("User id is null or empty");
        }

        Date claimTime = commandContext.getProcessEngineConfiguration().getClock().getCurrentTime();
        HashMap<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("userGroups", userGroups);
        params.put("claimTime", claimTime);

        int updatedRows = commandContext.getDbSqlSession().update("claimNextUnassignedCandidateTask", params);
        return updatedRows > 0;
    }
}
