package org.activiti.engine.impl.persistence;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.activiti.engine.impl.persistence.entity.ByteArrayRef;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeReference;

/**
 * MyBatis TypeHandler for {@link ByteArrayRef}.
 * 
 * @author Marcus Klimstra (CGI)
 */
public class ByteArrayRefTypeHandler extends TypeReference<ByteArrayRef> implements TypeHandler<ByteArrayRef> {

  @Override
  public void setParameter(PreparedStatement ps, int i, ByteArrayRef parameter, JdbcType jdbcType) throws SQLException {
    ps.setString(i, getValueToSet(parameter));
  }

  private String getValueToSet(ByteArrayRef parameter) {
    if (parameter == null) {
      // Note that this should not happen: ByteArrayRefs should always be initialized.
      return null;
    }
    return parameter.getId();
  }
  
  @Override
  public ByteArrayRef getResult(ResultSet rs, String columnName) throws SQLException {
    String id = rs.getString(columnName);
    return new ByteArrayRef(id);
  }

  @Override
  public ByteArrayRef getResult(ResultSet rs, int columnIndex) throws SQLException {
    String id = rs.getString(columnIndex);
    return new ByteArrayRef(id);
  }

  @Override
  public ByteArrayRef getResult(CallableStatement cs, int columnIndex) throws SQLException {
    String id = cs.getString(columnIndex);
    return new ByteArrayRef(id);
  }

}
