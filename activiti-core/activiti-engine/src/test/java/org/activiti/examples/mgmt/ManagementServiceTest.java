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

package org.activiti.examples.mgmt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.activiti.engine.ManagementService;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.management.TableMetaData;

/**
 * Test case for the various operations of the {@link ManagementService}
 *
 */
public class ManagementServiceTest extends PluggableActivitiTestCase {

  public void testTableCount() {
    Map<String, Long> tableCount = managementService.getTableCount();

    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();

    assertThat(tableCount.get(tablePrefix + "ACT_GE_PROPERTY")).isEqualTo(Long.valueOf(4));
    assertThat(tableCount.get(tablePrefix + "ACT_GE_BYTEARRAY")).isEqualTo(Long.valueOf(0));
    assertThat(tableCount.get(tablePrefix + "ACT_RE_DEPLOYMENT")).isEqualTo(Long.valueOf(0));
    assertThat(tableCount.get(tablePrefix + "ACT_RU_EXECUTION")).isEqualTo(Long.valueOf(0));
    assertThat(tableCount.get(tablePrefix + "ACT_RE_PROCDEF")).isEqualTo(Long.valueOf(0));
    assertThat(tableCount.get(tablePrefix + "ACT_RU_TASK")).isEqualTo(Long.valueOf(0));
    assertThat(tableCount.get(tablePrefix + "ACT_RU_IDENTITYLINK")).isEqualTo(Long.valueOf(0));
  }

  public void testGetTableMetaData() {

    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();

    TableMetaData tableMetaData = managementService.getTableMetaData(tablePrefix + "ACT_RU_TASK");
    assertThat(tableMetaData.getColumnTypes()).hasSize(tableMetaData.getColumnNames().size());
    assertThat(tableMetaData.getColumnNames()).hasSize(22);

    int assigneeIndex = tableMetaData.getColumnNames().indexOf("ASSIGNEE_");
    int createTimeIndex = tableMetaData.getColumnNames().indexOf("CREATE_TIME_");

    assertThat(assigneeIndex).isGreaterThanOrEqualTo(0);
    assertThat(createTimeIndex).isGreaterThanOrEqualTo(0);

    assertThat(tableMetaData.getColumnTypes().get(assigneeIndex)).isIn("VARCHAR", "NVARCHAR2", "nvarchar", "NVARCHAR","CHARACTER VARYING");
    assertThat(tableMetaData.getColumnTypes().get(createTimeIndex)).isIn("TIMESTAMP", "TIMESTAMP(6)", "datetime", "DATETIME");
  }

}
