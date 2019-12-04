package org.activiti.core.common.spring.security.policies;

import java.util.List;

public class SecurityPolicy {
    private String name;
    private List<String> groups;
    private List<String> users;
    private String serviceName;
    private SecurityPolicyAccess access;
    private List<String> keys;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public SecurityPolicyAccess getAccess() {
        return access;
    }

    public void setAccess(SecurityPolicyAccess access) {
        this.access = access;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }
}
