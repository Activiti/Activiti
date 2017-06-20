package activiti.mappers;

import org.apache.ibatis.annotations.Select;

import java.util.List;

/**

 */
public interface CustomMybatisMapper {

    @Select("SELECT ID_ FROM ACT_RE_PROCDEF WHERE KEY_ = #{key}")
    String loadProcessDefinitionIdByKey(String key);
}
