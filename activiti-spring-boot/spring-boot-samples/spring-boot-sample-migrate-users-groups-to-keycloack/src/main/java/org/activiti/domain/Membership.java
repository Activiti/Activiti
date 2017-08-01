package org.activiti.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "ACT_ID_MEMBERSHIP")
@IdClass(MembershipId.class)
public class Membership {

    @Id
    @Column(name = "USER_ID_")
    private String userId;
    @Id
    @Column(name = "GROUP_ID_")
    private String groupId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

}