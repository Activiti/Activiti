package liquibase.ext;

import liquibase.database.structure.type.BooleanType;
import liquibase.database.structure.type.DataType;
import liquibase.database.structure.type.IntType;
import liquibase.database.structure.type.NVarcharType;
import liquibase.database.structure.type.VarcharType;
import liquibase.database.typeconversion.core.H2TypeConverter;

public class ActivitiH2TypeConverter extends H2TypeConverter {
  
  @Override
  public int getPriority() {
    return PRIORITY_DATABASE + 1;
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
  public BooleanType getBooleanType() {
    return new BooleanType("bit");
  }

  @Override
  public IntType getIntType() {
    return new IntType("integer");
  }
}
