package org.activiti.springdatajpa.pojo;
// Generated Nov 21, 2015 11:41:58 AM by Hibernate Tools 3.2.2.GA


import javax.persistence.*;
import java.util.Date;

/**
 * Attachment generated by hbm2java
 */
@Entity
@Table(name = "act_hi_attachment"
        , schema = "public"
)
public class Attachment implements java.io.Serializable {


    private String id;
    private Integer rev;
    private String userId;
    private String name;
    private String description;
    private String type;
    private String taskId;
    private String procInstId;
    private String url;
    private String contentId;
    private Date time;

    public Attachment() {
    }


    public Attachment(String id) {
        this.id = id;
    }

    public Attachment(String id, Integer rev, String userId, String name, String description, String type, String taskId, String procInstId, String url, String contentId, Date time) {
        this.id = id;
        this.rev = rev;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.taskId = taskId;
        this.procInstId = procInstId;
        this.url = url;
        this.contentId = contentId;
        this.time = time;
    }

    @Id

    @Column(name = "id_", unique = true, nullable = false, length = 64)
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "rev_")
    public Integer getRev() {
        return this.rev;
    }

    public void setRev(Integer rev) {
        this.rev = rev;
    }

    @Column(name = "user_id_")
    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Column(name = "name_")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "description_", length = 4000)
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "type_")
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(name = "task_id_", length = 64)
    public String getTaskId() {
        return this.taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Column(name = "proc_inst_id_", length = 64)
    public String getProcInstId() {
        return this.procInstId;
    }

    public void setProcInstId(String procInstId) {
        this.procInstId = procInstId;
    }

    @Column(name = "url_", length = 4000)
    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Column(name = "content_id_", length = 64)
    public String getContentId() {
        return this.contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "time_", length = 29)
    public Date getTime() {
        return this.time;
    }

    public void setTime(Date time) {
        this.time = time;
    }


}


