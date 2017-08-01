package org.activiti.service;

import java.util.List;

import org.activiti.domain.Group;
import org.activiti.domain.User;

public interface UserService {
    List<User> loadAll();
    List<String> loadGroupsByUserId(String userId);

}
