package org.activiti.spring;

import org.activiti.engine.impl.asyncexecutor.DefaultJobManager;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.TransactionListener;
import org.activiti.engine.impl.cfg.TransactionState;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.JobEntity;

public class SpringJobManager extends DefaultJobManager {

    public SpringJobManager() {
        super(null);
    }

    public SpringJobManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
        super(processEngineConfiguration);
    }

    @Override
    protected void hintAsyncExecutor(final JobEntity job) {
        Context.getTransactionContext().addTransactionListener(TransactionState.COMMITTED, new TransactionListener() {
            public void execute(CommandContext commandContext) {
                getAsyncExecutor().executeAsyncJob(job);
            }
        });
    }

}
