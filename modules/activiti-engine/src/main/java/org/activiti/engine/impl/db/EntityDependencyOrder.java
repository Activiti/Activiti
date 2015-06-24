package org.activiti.engine.impl.db;

import org.activiti.engine.impl.persistence.entity.ByteArrayEntity;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.MembershipEntity;
import org.activiti.engine.impl.persistence.entity.ModelEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;

/**
 * Maintains a list of all the entities in order of dependency.
 * For eg. If ByteArray.java will appear before VariableInstance.java,
 *      since VariableInstance has ByteArray.id as FK
 */
public enum EntityDependencyOrder {
  DeploymentEntity(DeploymentEntity.class),
  ByteArrayEntity(ByteArrayEntity.class),
  ModelEntity(ModelEntity.class),
  UserEntity(UserEntity.class),
  GroupEntity(GroupEntity.class),
  MembershipEntity(MembershipEntity.class),
  ProcessDefinitionEntity(ProcessDefinitionEntity.class),
  ExecutionEntity(ExecutionEntity.class),
  EventSubscriptionEntity(EventSubscriptionEntity.class),
  TaskEntity(TaskEntity.class),
  IdentityLinkEntity(IdentityLinkEntity.class),
  JobEntity(JobEntity.class),
  VariableInstanceEntity(VariableInstanceEntity.class);

  private Class<? extends PersistentObject> clazz;

  private EntityDependencyOrder(Class<? extends PersistentObject> clazz) {
    this.clazz = clazz;
  }

  public Class<? extends PersistentObject> getClazz() {
    return clazz;
  }

}
