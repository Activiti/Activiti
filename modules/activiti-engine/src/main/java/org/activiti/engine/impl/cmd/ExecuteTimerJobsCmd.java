package org.activiti.engine.impl.cmd;

import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class ExecuteTimerJobsCmd implements Command<Void> {

    private final AsyncExecutor asyncExecutor;

    private final TimerJobEntity job;

    public ExecuteTimerJobsCmd(AsyncExecutor asyncExecutor, TimerJobEntity job) {
        this.asyncExecutor = asyncExecutor;
        this.job = job;
    }

    public Void execute(CommandContext commandContext) {
        lockJob(commandContext, job, asyncExecutor.getAsyncJobLockTimeInMillis());
        asyncExecutor.getProcessEngineConfiguration().getJobManager().moveTimerJobToExecutableJob(job);
        return null;
    }

    protected void lockJob(CommandContext commandContext, TimerJobEntity job, int lockTimeInMillis) {

        // This will trigger an optimistic locking exception when two concurrent executors
        // try to lock, as the revision will not match.

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(commandContext.getProcessEngineConfiguration().getClock().getCurrentTime());
        gregorianCalendar.add(Calendar.MILLISECOND, lockTimeInMillis);
        job.setLockOwner(asyncExecutor.getLockOwner());
        job.setLockExpirationTime(gregorianCalendar.getTime());
    }
}
