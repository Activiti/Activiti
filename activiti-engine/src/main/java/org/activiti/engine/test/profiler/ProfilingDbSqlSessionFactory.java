package org.activiti.engine.test.profiler;

import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.Session;

/**

 */
public class ProfilingDbSqlSessionFactory extends DbSqlSessionFactory {

    @Override
    public Session openSession(CommandContext commandContext) {
        return new ProfilingDbSqlSession(this, commandContext.getEntityCache());
    }

}
