package org.activiti.service;

import java.util.List;

import org.activiti.domain.Group;
import org.activiti.domain.User;
import org.activiti.repositories.GroupRepository;
import org.activiti.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> loadAll() {
      return userRepository.findAll();
    }

    @Override
    public List<String> loadGroupsByUserId(String userId) {
        return userRepository.findGroupsById(userId);
    }

}
