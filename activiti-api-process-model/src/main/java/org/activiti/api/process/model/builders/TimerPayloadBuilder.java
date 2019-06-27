package org.activiti.api.process.model.builders;

import java.util.Date;

import org.activiti.api.process.model.payloads.TimerPayload;

public class TimerPayloadBuilder {
    private Date dueDate;
    private Date endDate;

    private String executionId;
    private boolean isExclusive;

    private int retries;   
    
    private int maxIterations;
    private String repeat;

    protected String jobHandlerType;
    protected String jobHandlerConfiguration;
    
    protected String exceptionMessage;

    protected String tenantId;
    protected String jobType; 
    
    public TimerPayloadBuilder withDueDate(Date dueDate) {
        this.dueDate = dueDate;
        return this;
    }
    
    public TimerPayloadBuilder withEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }
    
    public TimerPayloadBuilder withExecutionId(String executionId) {
        this.executionId = executionId;
        return this;
    }
    
    public TimerPayloadBuilder withIsExclusive(boolean isExclusive) {
        this.isExclusive = isExclusive;
        return this;
    }
    
    public TimerPayloadBuilder withRetries(int retries) {
        this.retries = retries;
        return this;
    }
    
    public TimerPayloadBuilder withMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
        return this;
    }

    public TimerPayloadBuilder withRepeat(String repeat) {
        this.repeat = repeat;
        return this;
    }
 
    public TimerPayloadBuilder withJobHandlerType(String jobHandlerType) {
        this.jobHandlerType = jobHandlerType;
        return this;
    }
    
    public TimerPayloadBuilder withJobHandlerConfiguration(String jobHandlerConfiguration) {
        this.jobHandlerConfiguration = jobHandlerConfiguration;
        return this;
    }

    public TimerPayloadBuilder withExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
        return this;
    }

    public TimerPayloadBuilder withTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }
 
    public TimerPayloadBuilder withJobType(String jobType) {
        this.jobType = jobType;
        return this;
    }
    
    public TimerPayload build() {
        TimerPayload timerPayload = new TimerPayload();
        
        timerPayload.setDuedate(dueDate) ;
        timerPayload.setEndDate(endDate);
        timerPayload.setExecutionId(executionId);
        timerPayload.setExclusive(isExclusive);
        timerPayload.setRetries(retries);
        timerPayload.setMaxIterations(maxIterations);
        timerPayload.setRepeat(repeat);
        timerPayload.setJobHandlerType(jobHandlerType);
        timerPayload.setJobHandlerConfiguration(jobHandlerConfiguration);
        timerPayload.setExceptionMessage(exceptionMessage);
        timerPayload.setTenantId(tenantId);
        timerPayload.setJobType(jobType);
        
        return timerPayload;
    }
}
