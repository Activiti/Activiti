package org.activiti.standalone.cfg;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Select;

public interface MyTestMapper {

	@Select("SELECT ID_ as ID, NAME_ as NAME, CREATE_TIME_ as CREATETIME FROM ACT_RU_TASK")
	List<Map<String, Object>> selectTasks();
	
	@Select({
		"SELECT task.ID_ as TASKID, variable.LONG_ as VARIABLEVALUE FROM ACT_RU_VARIABLE variable",
		"inner join ACT_RU_TASK task on variable.TASK_ID_ = task.ID_",
		"where variable.NAME_ = #{variableName}"
	})
	List<Map<String, Object>> selectTaskWithSpecificVariable(String variableName);
	
}
