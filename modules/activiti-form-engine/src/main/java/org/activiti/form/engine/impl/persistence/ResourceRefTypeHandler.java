package org.activiti.form.engine.impl.persistence;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.activiti.form.engine.impl.persistence.entity.ResourceRef;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeReference;

/**
 * MyBatis TypeHandler for {@link ResourceRef}.
 * 
 * @author Marcus Klimstra (CGI)
 */
public class ResourceRefTypeHandler extends TypeReference<ResourceRef> implements TypeHandler<ResourceRef> {

  @Override
  public void setParameter(PreparedStatement ps, int i, ResourceRef parameter, JdbcType jdbcType) throws SQLException {
    ps.setString(i, getValueToSet(parameter));
  }

  private String getValueToSet(ResourceRef parameter) {
    if (parameter == null) {
      // Note that this should not happen: ByteArrayRefs should always be initialized.
      return null;
    }
    return parameter.getId();
  }

  @Override
  public ResourceRef getResult(ResultSet rs, String columnName) throws SQLException {
    String id = rs.getString(columnName);
    return new ResourceRef(id);
  }

  @Override
  public ResourceRef getResult(ResultSet rs, int columnIndex) throws SQLException {
    String id = rs.getString(columnIndex);
    return new ResourceRef(id);
  }

  @Override
  public ResourceRef getResult(CallableStatement cs, int columnIndex) throws SQLException {
    String id = cs.getString(columnIndex);
    return new ResourceRef(id);
  }

}
