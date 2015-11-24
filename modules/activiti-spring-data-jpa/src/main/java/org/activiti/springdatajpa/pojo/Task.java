package org.activiti.springdatajpa.pojo;
// Generated Nov 21, 2015 11:41:58 AM by Hibernate Tools 3.2.2.GA


import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Task generated by hbm2java
 */
@Entity
@Table(name = "act_ru_task")
public class Task implements java.io.Serializable {

    private String id;
    private ProcessDefinition processDefinition;
    private Execution executionByExecutionId;
    private Execution executionByProcInstId;
    private Integer rev;
    private String name;
    private String parentTaskId;
    private String description;
    private String taskDefKey;
    private String owner;
    private String assignee;
    private String delegation;
    private Integer priority;
    private Date createTime;
    private Date dueDate;
    private String category;
    private Integer suspensionState;
    private String tenantId;
    private String formKey;
    private Set<IdentityLink> identityLinks = new HashSet<IdentityLink>(0);

    public Task() {
    }

    public Task(String id) {
        this.id = id;
    }

    public Task(String id, ProcessDefinition processDefinition, Execution executionByExecutionId, Execution executionByProcInstId, Integer rev, String name, String parentTaskId, String description, String taskDefKey, String owner, String assignee, String delegation, Integer priority, Date createTime, Date dueDate, String category, Integer suspensionState, String tenantId, String formKey, Set<IdentityLink> identityLinks) {
        this.id = id;
        this.processDefinition = processDefinition;
        this.executionByExecutionId = executionByExecutionId;
        this.executionByProcInstId = executionByProcInstId;
        this.rev = rev;
        this.name = name;
        this.parentTaskId = parentTaskId;
        this.description = description;
        this.taskDefKey = taskDefKey;
        this.owner = owner;
        this.assignee = assignee;
        this.delegation = delegation;
        this.priority = priority;
        this.createTime = createTime;
        this.dueDate = dueDate;
        this.category = category;
        this.suspensionState = suspensionState;
        this.tenantId = tenantId;
        this.formKey = formKey;
        this.identityLinks = identityLinks;
    }

    @Id
    @Column(name = "id_", unique = true, nullable = false, length = 64)
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proc_def_id_")
    public ProcessDefinition getProcessDefinition() {
        return this.processDefinition;
    }

    public void setProcessDefinition(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id_")
    public Execution getExecutionByExecutionId() {
        return this.executionByExecutionId;
    }

    public void setExecutionByExecutionId(Execution executionByExecutionId) {
        this.executionByExecutionId = executionByExecutionId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proc_inst_id_")
    public Execution getExecutionByProcInstId() {
        return this.executionByProcInstId;
    }

    public void setExecutionByProcInstId(Execution executionByProcInstId) {
        this.executionByProcInstId = executionByProcInstId;
    }

    @Column(name = "rev_")
    public Integer getRev() {
        return this.rev;
    }

    public void setRev(Integer rev) {
        this.rev = rev;
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

    @Column(name = "task_def_key_")
    public String getTaskDefKey() {
        return this.taskDefKey;
    }

    public void setTaskDefKey(String taskDefKey) {
        this.taskDefKey = taskDefKey;
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

    @Column(name = "delegation_", length = 64)
    public String getDelegation() {
        return this.delegation;
    }

    public void setDelegation(String delegation) {
        this.delegation = delegation;
    }

    @Column(name = "priority_")
    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time_", length = 29)
    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "due_date_", length = 29)
    public Date getDueDate() {
        return this.dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    @Column(name = "category_")
    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Column(name = "suspension_state_")
    public Integer getSuspensionState() {
        return this.suspensionState;
    }

    public void setSuspensionState(Integer suspensionState) {
        this.suspensionState = suspensionState;
    }

    @Column(name = "tenant_id_")
    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Column(name = "form_key_")
    public String getFormKey() {
        return this.formKey;
    }

    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "actRuTask")
    public Set<IdentityLink> getIdentityLinks() {
        return this.identityLinks;
    }

    public void setIdentityLinks(Set<IdentityLink> identityLinks) {
        this.identityLinks = identityLinks;
    }
}


