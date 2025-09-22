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

package org.activiti.engine.test.api.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test to verify that the ACT_IDX_TASK_PARENT_TASK_ID index improves performance
 * of subtask queries. This test creates multiple parent tasks with multiple subtasks
 * and verifies that the getSubTasks method works correctly.
 */
public class SubTaskPerformanceTest extends PluggableActivitiTestCase {

    private static Logger log = LoggerFactory.getLogger(SubTaskPerformanceTest.class);

    public void testSubTaskQueryPerformance() {
        List<String> parentTaskIds = new ArrayList<String>();
        List<String> allTaskIds = new ArrayList<String>();

        try {
            // Create multiple parent tasks, each with multiple subtasks
            int parentTaskCount = 10;
            int subTasksPerParent = 5;
            
            for (int i = 0; i < parentTaskCount; i++) {
                // Create parent task
                Task parentTask = taskService.newTask();
                parentTask.setName("Parent Task " + i);
                parentTask.setDescription("Parent task for performance testing");
                taskService.saveTask(parentTask);
                parentTaskIds.add(parentTask.getId());
                allTaskIds.add(parentTask.getId());
                
                log.info("Created parent task {} with ID {}", i, parentTask.getId());
                
                // Create subtasks for this parent
                for (int j = 0; j < subTasksPerParent; j++) {
                    Task subTask = taskService.newTask();
                    subTask.setName("SubTask " + i + "." + j);
                    subTask.setDescription("Subtask " + j + " of parent " + i);
                    subTask.setParentTaskId(parentTask.getId());
                    taskService.saveTask(subTask);
                    allTaskIds.add(subTask.getId());
                    
                    log.info("Created subtask {} for parent {}", subTask.getId(), parentTask.getId());
                }
            }
            
            // Now test querying subtasks for each parent
            for (int i = 0; i < parentTaskIds.size(); i++) {
                String parentTaskId = parentTaskIds.get(i);
                
                long startTime = System.nanoTime();
                List<Task> subTasks = taskService.getSubTasks(parentTaskId);
                long endTime = System.nanoTime();
                
                double durationMs = (endTime - startTime) / 1_000_000.0;
                log.info("Query for parent task {} took {} ms and found {} subtasks", 
                         i, durationMs, subTasks.size());
                
                // Verify we found the correct number of subtasks
                assertThat(subTasks.size()).isEqualTo(subTasksPerParent);
                
                // Verify all subtasks have the correct parent
                for (Task subTask : subTasks) {
                    assertThat(subTask.getParentTaskId()).isEqualTo(parentTaskId);
                    log.info("Verified subtask {} has parent {}", subTask.getName(), parentTaskId);
                }
            }
            
            log.info("Successfully completed performance test with {} parent tasks and {} subtasks each", 
                     parentTaskCount, subTasksPerParent);
            
        } finally {
            // Clean up all created tasks
            for (String taskId : allTaskIds) {
                try {
                    taskService.deleteTask(taskId, true);
                } catch (Exception e) {
                    log.warn("Failed to delete task {}: {}", taskId, e.getMessage());
                }
            }
            log.info("Cleaned up {} tasks", allTaskIds.size());
        }
    }
}