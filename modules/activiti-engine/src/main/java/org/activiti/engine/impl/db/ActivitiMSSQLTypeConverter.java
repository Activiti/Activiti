package org.activiti.engine.impl.db;

import liquibase.database.structure.type.BigIntType;
import liquibase.database.structure.type.DateTimeType;
import liquibase.database.structure.type.DoubleType;
import liquibase.database.structure.type.IntType;
import liquibase.database.typeconversion.core.MSSQLTypeConverter;

public class ActivitiMSSQLTypeConverter extends MSSQLTypeConverter {
  
  @Override
  public int getPriority() {
    return PRIORITY_DATABASE + 1;
  }
  
  /*@Override
  public BooleanType getBooleanType() {
    return new BooleanType("tinyint");
  }*/

  @Override
  public DateTimeType getDateTimeType() {
    return new DateTimeType("datetime");
  }

  @Override
  public IntType getIntType() {
    return new IntType("int");
  }
  
  @Override
  public BigIntType getBigIntType() {
    return new BigIntType("numeric(19,0)");
  }

  @Override
  public DoubleType getDoubleType() {
    return new DoubleType("double precision");
  }
}
