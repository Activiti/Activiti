package org.activiti.repositories;


import java.util.List;

import org.activiti.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, String> {
    
    @Query(value = "SELECT groupId FROM Membership  where userId = ?1")
    List<String> findGroupsById(String userId);
}
