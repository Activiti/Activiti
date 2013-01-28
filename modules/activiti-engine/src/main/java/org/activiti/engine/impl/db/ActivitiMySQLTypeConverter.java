package org.activiti.engine.impl.db;

import liquibase.database.structure.type.BlobType;
import liquibase.database.structure.type.BooleanType;
import liquibase.database.structure.type.DataType;
import liquibase.database.structure.type.DateTimeType;
import liquibase.database.structure.type.IntType;
import liquibase.database.structure.type.NVarcharType;
import liquibase.database.structure.type.VarcharType;
import liquibase.database.typeconversion.core.MySQLTypeConverter;

public class ActivitiMySQLTypeConverter extends MySQLTypeConverter {
  
  @Override
  public int getPriority() {
    return PRIORITY_DATABASE + 1;
  }
 
  @Override
  protected DataType getDataType(final String columnTypeString,
      final Boolean autoIncrement, final String dataTypeName,
      final String precision, final String additionalInformation) {

    if (columnTypeString.equalsIgnoreCase("timestamp")) {
      return new DateTimeType("datetime");
    }
    
    DataType dataType = super.getDataType(columnTypeString, autoIncrement,
        dataTypeName, precision, additionalInformation);
    if (dataType instanceof NVarcharType) {
      dataType = new VarcharType();
      dataType.setFirstParameter(precision);
    }
    return dataType;
  }
  
  @Override
  public BooleanType getBooleanType() {
    return new BooleanType("TINYINT");
  }

  @Override
  public IntType getIntType() {
    return new IntType("integer");
  }

  @Override
  public BlobType getBlobType() {
    return new BlobType("LONGBLOB");
  }
}
