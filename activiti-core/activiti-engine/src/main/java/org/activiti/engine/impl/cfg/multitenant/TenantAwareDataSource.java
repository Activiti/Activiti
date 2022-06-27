/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.activiti.engine.impl.cfg.multitenant;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.activiti.engine.ActivitiException;

/**
 * A {@link DataSource} implementation that switches the currently used datasource based on the
 * current values of the {@link TenantInfoHolder}.
 *
 * When a {@link Connection} is requested from this {@link DataSource}, the correct {@link DataSource}
 * for the current tenant will be determined and used.
 *
 * Heavily influenced and inspired by Spring's AbstractRoutingDataSource.
 *
 * @deprecated multi-tenant code will be removed in future version of Activiti and Activiti Cloud
 */
@Deprecated
public class TenantAwareDataSource implements DataSource {

  protected TenantInfoHolder tenantInfoHolder;
  protected Map<Object, DataSource> dataSources = new HashMap<Object, DataSource>();

  public TenantAwareDataSource(TenantInfoHolder tenantInfoHolder) {
    this.tenantInfoHolder = tenantInfoHolder;
  }

  public void addDataSource(Object key, DataSource dataSource) {
    dataSources.put(key, dataSource);
  }

  public void removeDataSource(Object key) {
    dataSources.remove(key);
  }

  public Connection getConnection() throws SQLException {
    return getCurrentDataSource().getConnection();
  }

  public Connection getConnection(String username, String password) throws SQLException {
    return  getCurrentDataSource().getConnection(username, password);
  }

  protected DataSource getCurrentDataSource() {
    String tenantId = tenantInfoHolder.getCurrentTenantId();
    DataSource dataSource = dataSources.get(tenantId);
    if (dataSource == null) {
      throw new ActivitiException("Could not find a dataSource for tenant " + tenantId);
    }
    return dataSource;
  }

  public int getLoginTimeout() throws SQLException {
    return 0; // Default
  }

  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  }

  @SuppressWarnings("unchecked")
  public <T> T unwrap(Class<T> iface) throws SQLException {
    if (iface.isInstance(this)) {
      return (T) this;
    }
    throw new SQLException("Cannot unwrap " + getClass().getName() + " as an instance of " + iface.getName());
  }

  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return iface.isInstance(this);
  }

  public Map<Object, DataSource> getDataSources() {
    return dataSources;
  }

  public void setDataSources(Map<Object, DataSource> dataSources) {
    this.dataSources = dataSources;
  }

  // Unsupported //////////////////////////////////////////////////////////

  public PrintWriter getLogWriter() throws SQLException {
    throw new UnsupportedOperationException();
  }

  public void setLogWriter(PrintWriter out) throws SQLException {
    throw new UnsupportedOperationException();
  }

  public void setLoginTimeout(int seconds) throws SQLException {
    throw new UnsupportedOperationException();
  }

}
