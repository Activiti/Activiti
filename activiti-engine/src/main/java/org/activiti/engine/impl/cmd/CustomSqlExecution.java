package org.activiti.engine.impl.cmd;

/**

 */
public interface CustomSqlExecution<Mapper, ResultType> {

  Class<Mapper> getMapperClass();

  ResultType execute(Mapper mapper);

}