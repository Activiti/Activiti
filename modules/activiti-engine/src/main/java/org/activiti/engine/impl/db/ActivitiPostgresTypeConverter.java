package org.activiti.engine.impl.db;

import liquibase.database.structure.type.DataType;
import liquibase.database.structure.type.DateTimeType;
import liquibase.database.structure.type.IntType;
import liquibase.database.structure.type.NVarcharType;
import liquibase.database.structure.type.VarcharType;
import liquibase.database.typeconversion.core.PostgresTypeConverter;

public class ActivitiPostgresTypeConverter extends PostgresTypeConverter {
  
  @Override
  public int getPriority() {
    return super.getPriority() + 1;
  }
 
  @Override
  protected DataType getDataType(final String columnTypeString,
      final Boolean autoIncrement, final String dataTypeName,
      final String precision, final String additionalInformation) {

    DataType dataType = super.getDataType(columnTypeString, autoIncrement,
        dataTypeName, precision, additionalInformation);
    if (dataType instanceof NVarcharType) {
      dataType = new VarcharType();
      dataType.setFirstParameter(precision);
    }
    return dataType;
  }

  @Override
  public IntType getIntType() {
    return new IntType("integer");
  }
  
  @Override
  public DateTimeType getDateTimeType() {
    return new DateTimeType("TIMESTAMP");
  }
}
