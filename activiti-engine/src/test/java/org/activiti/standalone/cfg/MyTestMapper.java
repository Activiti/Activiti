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
