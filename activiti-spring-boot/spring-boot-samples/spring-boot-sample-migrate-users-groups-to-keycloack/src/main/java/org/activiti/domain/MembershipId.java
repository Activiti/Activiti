package org.activiti.domain;

import java.io.Serializable;

public class MembershipId implements Serializable {

    private String userId;

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
