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

package org.activiti.engine.test.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test to verify that the ACT_IDX_TASK_PARENT_TASK_ID index is properly created.
 */
public class TaskParentIndexTest extends PluggableActivitiTestCase {

    private static Logger log = LoggerFactory.getLogger(TaskParentIndexTest.class);

    public void testTaskParentTaskIdIndexExists() {
        ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration()
            .getCommandExecutor()
            .execute(
                new Command<Object>() {
                    public Object execute(CommandContext commandContext) {
                        try {
                            SqlSession sqlSession = commandContext.getDbSqlSession().getSqlSession();
                            boolean indexFound = false;
                            
                            // Check for the index on ACT_RU_TASK table
                            ResultSet indexes = sqlSession
                                .getConnection()
                                .getMetaData()
                                .getIndexInfo(null, null, "ACT_RU_TASK", false, false);
                            
                            while (indexes.next()) {
                                String indexName = indexes.getString("INDEX_NAME");
                                String columnName = indexes.getString("COLUMN_NAME");
                                
                                log.info("Found index {} on column {}", indexName, columnName);
                                
                                if ("ACT_IDX_TASK_PARENT_TASK_ID".equalsIgnoreCase(indexName) && 
                                    "PARENT_TASK_ID_".equalsIgnoreCase(columnName)) {
                                    indexFound = true;
                                    log.info("Successfully found ACT_IDX_TASK_PARENT_TASK_ID index on PARENT_TASK_ID_ column");
                                    break;
                                }
                            }
                            indexes.close();
                            
                            assertThat(indexFound).as("ACT_IDX_TASK_PARENT_TASK_ID index should exist on PARENT_TASK_ID_ column").isTrue();
                            
                        } catch (Exception e) {
                            log.error("Error checking for index", e);
                            throw new RuntimeException("Failed to check for ACT_IDX_TASK_PARENT_TASK_ID index", e);
                        }
                        return null;
                    }
                }
            );
    }
}