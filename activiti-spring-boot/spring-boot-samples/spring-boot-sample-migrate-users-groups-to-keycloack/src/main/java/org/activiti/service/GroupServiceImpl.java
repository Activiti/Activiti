package org.activiti.service;

import java.util.List;

import org.activiti.domain.Group;
import org.activiti.repositories.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Override
    public List<Group> loadAll() {
      return groupRepository.findAll();
    }

}
