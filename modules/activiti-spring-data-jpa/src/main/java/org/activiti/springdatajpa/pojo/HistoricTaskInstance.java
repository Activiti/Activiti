package org.activiti.springdatajpa.pojo;
// Generated Nov 21, 2015 11:41:58 AM by Hibernate Tools 3.2.2.GA


import javax.persistence.*;
import java.util.Date;

/**
 * HistoricTaskInstance generated by hbm2java
 */
@Entity
@Table(name = "act_hi_taskinst")
public class HistoricTaskInstance implements java.io.Serializable {


    private String id;
    private String processDefinitionId;
    private String taskDefinitionKey;
    private String processInstanceId;
    private String executionId;
    private String name;
    private String parentTaskId;
    private String description;
    private String owner;
    private String assignee;
    private Date startTime;
    private Date claimTime;
    private Date endTime;
    private Long duration;
    private String deleteReason;
    private Integer priority;
    private Date dueDate;
    private String formKey;
    private String category;
    private String tenantId;

    public HistoricTaskInstance() {
    }


    public HistoricTaskInstance(String id, Date startTime) {
        this.id = id;
        this.startTime = startTime;
    }

    public HistoricTaskInstance(String id, String processDefinitionId, String taskDefinitionKey, String processInstanceId, String executionId, String name, String parentTaskId, String description, String owner, String assignee, Date startTime, Date claimTime, Date endTime, Long duration, String deleteReason, Integer priority, Date dueDate, String formKey, String category, String tenantId) {
        this.id = id;
        this.processDefinitionId = processDefinitionId;
        this.taskDefinitionKey = taskDefinitionKey;
        this.processInstanceId = processInstanceId;
        this.executionId = executionId;
        this.name = name;
        this.parentTaskId = parentTaskId;
        this.description = description;
        this.owner = owner;
        this.assignee = assignee;
        this.startTime = startTime;
        this.claimTime = claimTime;
        this.endTime = endTime;
        this.duration = duration;
        this.deleteReason = deleteReason;
        this.priority = priority;
        this.dueDate = dueDate;
        this.formKey = formKey;
        this.category = category;
        this.tenantId = tenantId;
    }

    @Id
    @Column(name = "id_", unique = true, nullable = false, length = 64)
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "proc_def_id_", length = 64)
    public String getProcessDefinitionId() {
        return this.processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Column(name = "task_def_key_")
    public String getTaskDefinitionKey() {
        return this.taskDefinitionKey;
    }

    public void setTaskDefinitionKey(String taskDefinitionKey) {
        this.taskDefinitionKey = taskDefinitionKey;
    }

    @Column(name = "proc_inst_id_", length = 64)
    public String getProcessInstanceId() {
        return this.processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Column(name = "execution_id_", length = 64)
    public String getExecutionId() {
        return this.executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    @Column(name = "name_")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "parent_task_id_", length = 64)
    public String getParentTaskId() {
        return this.parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    @Column(name = "description_", length = 4000)
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "owner_")
    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Column(name = "assignee_")
    public String getAssignee() {
        return this.assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time_", nullable = false, length = 29)
    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "claim_time_", length = 29)
    public Date getClaimTime() {
        return this.claimTime;
    }

    public void setClaimTime(Date claimTime) {
        this.claimTime = claimTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time_", length = 29)
    public Date getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Column(name = "duration_")
    public Long getDuration() {
        return this.duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    @Column(name = "delete_reason_", length = 4000)
    public String getDeleteReason() {
        return this.deleteReason;
    }

    public void setDeleteReason(String deleteReason) {
        this.deleteReason = deleteReason;
    }

    @Column(name = "priority_")
    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "due_date_", length = 29)
    public Date getDueDate() {
        return this.dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    @Column(name = "form_key_")
    public String getFormKey() {
        return this.formKey;
    }

    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }

    @Column(name = "category_")
    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Column(name = "tenant_id_")
    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }


}


