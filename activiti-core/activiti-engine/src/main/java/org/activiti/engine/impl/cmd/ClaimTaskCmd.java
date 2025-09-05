/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

import org.activiti.engine.ActivitiTaskAlreadyClaimedException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

/**

 */
public class ClaimTaskCmd extends NeedsActiveTaskCmd<Void> {

    private static final long serialVersionUID = 1L;

    protected String newAssignee;
    protected boolean force;

    public ClaimTaskCmd(String taskId, String newAssigneeId) {
        super(taskId);
        this.newAssignee = newAssigneeId;
    }

    public ClaimTaskCmd(
        String taskId,
        String newAssigneeId,
        boolean force
    ) {
        super(taskId);
        this.newAssignee = newAssigneeId;
        this.force = force;
    }

    protected Void execute(CommandContext commandContext, TaskEntity task) {
        String existingAssignee = task.getAssignee();
        if (newAssignee == null) {
            task.setClaimTime(null);
            commandContext
                .getTaskEntityManager()
                .changeTaskAssignee(task, null);
        } else if (
            !force && existingAssignee != null && !newAssignee.equals(existingAssignee)
        ) {
            throw new ActivitiTaskAlreadyClaimedException(
                task.getId(),
                task.getAssignee()
            );
        } else {
            task.setClaimTime(
                commandContext
                    .getProcessEngineConfiguration()
                    .getClock()
                    .getCurrentTime()
            );
            commandContext
                .getTaskEntityManager()
                .changeTaskAssignee(task, newAssignee);
        }

        // Add claim time to historic task instance
        commandContext.getHistoryManager().recordTaskClaim(task);
        return null;
    }

    @Override
    protected String getSuspendedTaskException() {
        return "Cannot claim a suspended task";
    }
}
