package org.activiti.api.process.model.payloads;

import java.util.Date;
import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class TimerPayload implements Payload {

    private String id;
    
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

    public TimerPayload() {
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public String getId() {
        return id;
    }
    
    public Date getDuedate() {
        return dueDate;
    }

    
    public void setDuedate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    
    public String getExecutionId() {
        return executionId;
    }

    
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }
    
    public boolean isExclusive() {
        return isExclusive;
    }

    
    public void setExclusive(boolean isExclusive) {
        this.isExclusive = isExclusive;
    }

    
    public int getRetries() {
        return retries;
    }

    
    public void setRetries(int retries) {
        this.retries = retries;
    }

    
    public int getMaxIterations() {
        return maxIterations;
    }

    
    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    
    public String getRepeat() {
        return repeat;
    }

    
    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    
    public String getJobHandlerType() {
        return jobHandlerType;
    }

    
    public void setJobHandlerType(String jobHandlerType) {
        this.jobHandlerType = jobHandlerType;
    }

    
    public String getJobHandlerConfiguration() {
        return jobHandlerConfiguration;
    }

    
    public void setJobHandlerConfiguration(String jobHandlerConfiguration) {
        this.jobHandlerConfiguration = jobHandlerConfiguration;
    }

    
    public String getExceptionMessage() {
        return exceptionMessage;
    }

    
    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    
    public String getTenantId() {
        return tenantId;
    }

    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    
    public String getJobType() {
        return jobType;
    }

    
    public void setJobType(String jobType) {
        this.jobType = jobType;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dueDate == null) ? 0 : dueDate.hashCode());
        result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
        result = prime * result + ((exceptionMessage == null) ? 0 : exceptionMessage.hashCode());
        result = prime * result + ((executionId == null) ? 0 : executionId.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (isExclusive ? 1231 : 1237);
        result = prime * result + ((jobHandlerConfiguration == null) ? 0 : jobHandlerConfiguration.hashCode());
        result = prime * result + ((jobHandlerType == null) ? 0 : jobHandlerType.hashCode());
        result = prime * result + ((jobType == null) ? 0 : jobType.hashCode());
        result = prime * result + maxIterations;
        result = prime * result + ((repeat == null) ? 0 : repeat.hashCode());
        result = prime * result + retries;
        result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TimerPayload other = (TimerPayload) obj;
        if (dueDate == null) {
            if (other.dueDate != null)
                return false;
        } else if (!dueDate.equals(other.dueDate))
            return false;
        if (endDate == null) {
            if (other.endDate != null)
                return false;
        } else if (!endDate.equals(other.endDate))
            return false;
        if (exceptionMessage == null) {
            if (other.exceptionMessage != null)
                return false;
        } else if (!exceptionMessage.equals(other.exceptionMessage))
            return false;
        if (executionId == null) {
            if (other.executionId != null)
                return false;
        } else if (!executionId.equals(other.executionId))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (isExclusive != other.isExclusive)
            return false;
        if (jobHandlerConfiguration == null) {
            if (other.jobHandlerConfiguration != null)
                return false;
        } else if (!jobHandlerConfiguration.equals(other.jobHandlerConfiguration))
            return false;
        if (jobHandlerType == null) {
            if (other.jobHandlerType != null)
                return false;
        } else if (!jobHandlerType.equals(other.jobHandlerType))
            return false;
        if (jobType == null) {
            if (other.jobType != null)
                return false;
        } else if (!jobType.equals(other.jobType))
            return false;
        if (maxIterations != other.maxIterations)
            return false;
        if (repeat == null) {
            if (other.repeat != null)
                return false;
        } else if (!repeat.equals(other.repeat))
            return false;
        if (retries != other.retries)
            return false;
        if (tenantId == null) {
            if (other.tenantId != null)
                return false;
        } else if (!tenantId.equals(other.tenantId))
            return false;
        return true;
    }
    
}
