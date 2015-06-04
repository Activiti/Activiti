package org.activiti5.engine.impl.cmd;

/**
 * @author jbarrez
 */
public interface CustomSqlExecution<Mapper, ResultType> {
	
	Class<Mapper> getMapperClass();

	ResultType execute(Mapper mapper);

}