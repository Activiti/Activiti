package org.activiti.standalone.jta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

import bitronix.tm.resource.common.ResourceBean;
import bitronix.tm.resource.jdbc.PoolingDataSource;

/**
 * FactoryBean for Bitronix PoolingDataSource to correctly manage its lifecycle when used in JUnit tests.
 * 

 */
public class BitronixDataSourceFactoryBean extends ResourceBean implements FactoryBean<PoolingDataSource>, DisposableBean {
  private static final Logger LOG = LoggerFactory.getLogger(BitronixDataSourceFactoryBean.class);

  private PoolingDataSource ds;

  @Override
  public Class<?> getObjectType() {
    return PoolingDataSource.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  @Override
  public PoolingDataSource getObject() throws Exception {
    if (ds == null) {
      ds = new PoolingDataSource();
      ds.setClassName(getClassName());
      ds.setUniqueName(getUniqueName() + "_" + System.currentTimeMillis());
      ds.setAutomaticEnlistingEnabled(getAutomaticEnlistingEnabled());
      ds.setUseTmJoin(getUseTmJoin());
      ds.setMinPoolSize(getMinPoolSize());
      ds.setMaxPoolSize(getMaxPoolSize());
      ds.setMaxIdleTime(getMaxIdleTime());
      ds.setAcquireIncrement(getAcquireIncrement());
      ds.setAcquisitionTimeout(getAcquisitionTimeout());
      ds.setAcquisitionInterval(getAcquisitionInterval());
      ds.setDeferConnectionRelease(getDeferConnectionRelease());
      ds.setAllowLocalTransactions(getAllowLocalTransactions());
      ds.setShareTransactionConnections(getShareTransactionConnections());
      ds.setDisabled(isDisabled());
      ds.setIgnoreRecoveryFailures(getIgnoreRecoveryFailures());
      ds.setDriverProperties(getDriverProperties());

      LOG.debug("Initializing PoolingDataSource with id " + ds.getUniqueName());
      ds.init();
    }
    return ds;
  }

  @Override
  public void destroy() throws Exception {
    LOG.debug("Closing PoolingDataSource with id " + ds.getUniqueName());
    ds.close();
    ds = null;
  }
}
