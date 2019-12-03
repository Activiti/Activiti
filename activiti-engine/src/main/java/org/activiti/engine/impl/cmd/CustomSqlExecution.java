package org.activiti.engine.impl.cmd;

import org.activiti.engine.api.internal.Internal;

/**

 */
@Internal
public interface CustomSqlExecution<Mapper, ResultType> {

  Class<Mapper> getMapperClass();

  ResultType execute(Mapper mapper);

}