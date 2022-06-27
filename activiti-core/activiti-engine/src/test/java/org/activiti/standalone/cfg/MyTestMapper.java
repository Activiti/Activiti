/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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
package org.activiti.standalone.cfg;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Select;

public interface MyTestMapper {

  @Select("SELECT ID_ as id, NAME_ as name, CREATE_TIME_ as createTime FROM ACT_RU_TASK")
  List<Map<String, Object>> selectTasks();

  @Select({ "SELECT task.ID_ as taskId, variable.LONG_ as variableValue FROM ACT_RU_VARIABLE variable", "inner join ACT_RU_TASK task on variable.TASK_ID_ = task.ID_",
      "where variable.NAME_ = #{variableName}" })
  List<Map<String, Object>> selectTaskWithSpecificVariable(String variableName);

}
