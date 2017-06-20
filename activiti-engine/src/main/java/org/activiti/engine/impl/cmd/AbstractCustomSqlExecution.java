package org.activiti.engine.impl.cmd;

/**

 */
public abstract class AbstractCustomSqlExecution<Mapper, ResultType> implements CustomSqlExecution<Mapper, ResultType> {

  protected Class<Mapper> mapperClass;

  public AbstractCustomSqlExecution(Class<Mapper> mapperClass) {
    this.mapperClass = mapperClass;
  }

  @Override
  public Class<Mapper> getMapperClass() {
    return mapperClass;
  }

}
