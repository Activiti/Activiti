package org.activiti.springdatajpa.pojo;
// Generated Nov 21, 2015 11:41:58 AM by Hibernate Tools 3.2.2.GA


import javax.persistence.*;
import java.util.Date;

/**
 * HistoricVariableInstance generated by hbm2java
 */
@Entity
@Table(name = "act_hi_varinst"
        , schema = "public"
)
public class HistoricVariableInstance implements java.io.Serializable {


    private String id;
    private String processInstanceId;
    private String executionId;
    private String taskId;
    private String name;
    private String varType;
    private Integer rev;
    private String bytearrayId;
    private Double double_;
    private Long long_;
    private String text;
    private String text2;
    private Date createTime;
    private Date lastUpdatedTime;

    public HistoricVariableInstance() {
    }


    public HistoricVariableInstance(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public HistoricVariableInstance(String id, String processInstanceId, String executionId, String taskId, String name, String varType, Integer rev, String bytearrayId, Double double_, Long long_, String text, String text2, Date createTime, Date lastUpdatedTime) {
        this.id = id;
        this.processInstanceId = processInstanceId;
        this.executionId = executionId;
        this.taskId = taskId;
        this.name = name;
        this.varType = varType;
        this.rev = rev;
        this.bytearrayId = bytearrayId;
        this.double_ = double_;
        this.long_ = long_;
        this.text = text;
        this.text2 = text2;
        this.createTime = createTime;
        this.lastUpdatedTime = lastUpdatedTime;
    }

    @Id
    @Column(name = "id_", unique = true, nullable = false, length = 64)
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Column(name = "task_id_", length = 64)
    public String getTaskId() {
        return this.taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Column(name = "name_", nullable = false)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "var_type_", length = 100)
    public String getVarType() {
        return this.varType;
    }

    public void setVarType(String varType) {
        this.varType = varType;
    }

    @Column(name = "rev_")
    public Integer getRev() {
        return this.rev;
    }

    public void setRev(Integer rev) {
        this.rev = rev;
    }

    @Column(name = "bytearray_id_", length = 64)
    public String getBytearrayId() {
        return this.bytearrayId;
    }

    public void setBytearrayId(String bytearrayId) {
        this.bytearrayId = bytearrayId;
    }

    @Column(name = "double_", precision = 17, scale = 17)
    public Double getDouble_() {
        return this.double_;
    }

    public void setDouble_(Double double_) {
        this.double_ = double_;
    }

    @Column(name = "long_")
    public Long getLong_() {
        return this.long_;
    }

    public void setLong_(Long long_) {
        this.long_ = long_;
    }

    @Column(name = "text_", length = 4000)
    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Column(name = "text2_", length = 4000)
    public String getText2() {
        return this.text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
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
    @Column(name = "last_updated_time_", length = 29)
    public Date getLastUpdatedTime() {
        return this.lastUpdatedTime;
    }

    public void setLastUpdatedTime(Date lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }


}


