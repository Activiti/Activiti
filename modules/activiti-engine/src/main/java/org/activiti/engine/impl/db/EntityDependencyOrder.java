package org.activiti.engine.impl.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.activiti.engine.impl.persistence.entity.AttachmentEntity;
import org.activiti.engine.impl.persistence.entity.ByteArrayEntity;
import org.activiti.engine.impl.persistence.entity.CommentEntity;
import org.activiti.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricDetailAssignmentEntity;
import org.activiti.engine.impl.persistence.entity.HistoricDetailEntity;
import org.activiti.engine.impl.persistence.entity.HistoricDetailTransitionInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.activiti.engine.impl.persistence.entity.HistoricFormPropertyEntity;
import org.activiti.engine.impl.persistence.entity.HistoricIdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricScopeInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.activiti.engine.impl.persistence.entity.IdentityInfoEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.MembershipEntity;
import org.activiti.engine.impl.persistence.entity.MessageEntity;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ModelEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.PropertyEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;


/**
 * Maintains a list of all the entities in order of dependency.
 */
public class EntityDependencyOrder {
	
	public static List<Class<? extends PersistentObject>> DELETE_ORDER = new ArrayList<Class<? extends PersistentObject>>();
	public static List<Class<? extends PersistentObject>> INSERT_ORDER = new ArrayList<Class<? extends PersistentObject>>();
	
	static {
		
		/*
		 * In the comments below:
		 * 
		 * 'FK to X' : X should be BELOW the entity
		 * 
		 * 'FK from X': X should be ABOVE the entity
		 * 
		 */
	
		/* No FK */
		DELETE_ORDER.add(PropertyEntity.class);
		
		/* No FK */
		DELETE_ORDER.add(AttachmentEntity.class);
		
		/* No FK */
		DELETE_ORDER.add(CommentEntity.class);
		
		/* No FK */
		DELETE_ORDER.add(EventLogEntryEntity.class);
		
		/*
		 * FK to Deployment
		 * FK to ByteArray 
		 */
		DELETE_ORDER.add(ModelEntity.class); 
		
		/* Subclass of JobEntity */
		DELETE_ORDER.add(MessageEntity.class);
		
		/* Subclass of TimerEntity */
		DELETE_ORDER.add(TimerEntity.class);
		
		/*
		 * FK to ByteArray
		 */
		DELETE_ORDER.add(JobEntity.class);
		
		/*
		 * FK to ByteArray
		 * FK to Exeution
		 */
		DELETE_ORDER.add(VariableInstanceEntity.class);
		
		/*
		 * FK from ModelEntity
		 * FK from JobEntity
		 * FK from VariableInstanceEntity
		 * 
		 * FK to DeploymentEntity
		 */
		DELETE_ORDER.add(ByteArrayEntity.class);
		
		/*
		 * FK from ModelEntity
		 * FK from JobEntity
		 * FK from VariableInstanceEntity
		 * 
		 * FK to DeploymentEntity
		 */
		DELETE_ORDER.add(ResourceEntity.class);
		
		/*
		 * FK from ByteArray
		 */
		DELETE_ORDER.add(DeploymentEntity.class);
		
		/*
		 * FK to Execution
		 */
		DELETE_ORDER.add(EventSubscriptionEntity.class);
		
		/*
		 * FK to Execution
		 */
		DELETE_ORDER.add(CompensateEventSubscriptionEntity.class);
		
		/*
		 * FK to Execution
		 */
		DELETE_ORDER.add(MessageEventSubscriptionEntity.class);
		
		/*
		 * FK to Execution
		 */
		DELETE_ORDER.add(SignalEventSubscriptionEntity.class);
		
		
		/*
		 * FK to process definition
		 * FK to Execution
		 * FK to Task
		 */
		DELETE_ORDER.add(IdentityLinkEntity.class);
		
		/*
		 * FK from IdentityLink
		 * 
		 * FK to Execution
		 * FK to process definition
		 */
		DELETE_ORDER.add(TaskEntity.class);
		
		/*
		 * FK from VariableInstance 
		 * FK from EventSubscription
		 * FK from IdentityLink
		 * FK from Task
		 * 
		 * FK to ProcessDefinition
		 */
		DELETE_ORDER.add(ExecutionEntity.class);
		
		/*
		 * FK from Task
		 * FK from IdentityLink
		 * FK from execution
		 */
		DELETE_ORDER.add(ProcessDefinitionEntity.class);
		
		/*
	   * FK to User
	   * FK to Group
	   */
		DELETE_ORDER.add(MembershipEntity.class);
		
		/*
		 * Fk from Membership
		 */
		DELETE_ORDER.add(UserEntity.class);
	  
	  /*
	   * FK from Membership
	   */
		DELETE_ORDER.add(GroupEntity.class);
	  
	  
	  // History entities have no FK's
	  
		DELETE_ORDER.add(HistoricIdentityLinkEntity.class);
	  
		DELETE_ORDER.add(IdentityInfoEntity.class);
	  
		DELETE_ORDER.add(HistoricActivityInstanceEntity.class);
		DELETE_ORDER.add(HistoricProcessInstanceEntity.class);
		DELETE_ORDER.add(HistoricTaskInstanceEntity.class);
		DELETE_ORDER.add(HistoricScopeInstanceEntity.class);
	  
		DELETE_ORDER.add(HistoricVariableInstanceEntity.class);
	  
		DELETE_ORDER.add(HistoricDetailAssignmentEntity.class);
		DELETE_ORDER.add(HistoricDetailTransitionInstanceEntity.class);
		DELETE_ORDER.add(HistoricDetailVariableInstanceUpdateEntity.class);
		DELETE_ORDER.add(HistoricFormPropertyEntity.class);
		DELETE_ORDER.add(HistoricDetailEntity.class);
		
		INSERT_ORDER = new ArrayList<Class<? extends PersistentObject>>(DELETE_ORDER);
		Collections.reverse(INSERT_ORDER);

	}

}
