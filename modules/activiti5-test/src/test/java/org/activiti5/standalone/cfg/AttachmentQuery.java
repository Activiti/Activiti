/**
 * 
 */
package org.activiti5.standalone.cfg;

import java.util.List;

import org.activiti5.engine.ManagementService;
import org.activiti5.engine.impl.AbstractQuery;
import org.activiti5.engine.impl.Page;
import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.task.Attachment;

/**
 * @author Bassam Al-Sarori
 *
 */
public class AttachmentQuery extends AbstractQuery<AttachmentQuery, Attachment> {

  
  private static final long serialVersionUID = 1L;
  protected String attachmentId;
  protected String attachmentName;
  protected String attachmentType;
  protected String userId;
  protected String taskId;
  protected String processInstanceId;
  
  
  public AttachmentQuery(ManagementService managementService) {
    super(managementService);
  }
  
  public AttachmentQuery attachmentId(String attachmentId){
    this.attachmentId = attachmentId;
    return this;
  }
  
  public AttachmentQuery attachmentName(String attachmentName){
    this.attachmentName = attachmentName;
    return this;
  }
  
  public AttachmentQuery attachmentType(String attachmentType){
    this.attachmentType = attachmentType;
    return this;
  }
  
  public AttachmentQuery userId(String userId){
    this.userId = userId;
    return this;
  }
  
  public AttachmentQuery taskId(String taskId){
    this.taskId = taskId;
    return this;
  }
  
  public AttachmentQuery processInstanceId(String processInstanceId){
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  public AttachmentQuery orderByAttachmentId(){
    return orderBy(AttachmentQueryProperty.ATTACHMENT_ID);
  }
  
  public AttachmentQuery orderByAttachmentName(){
    return orderBy(AttachmentQueryProperty.NAME);
  }
  
  public AttachmentQuery orderByAttachmentCreateTime(){
    return orderBy(AttachmentQueryProperty.CREATE_TIME);
  }
  
  @Override
  public long executeCount(CommandContext commandContext) {
    return (Long) commandContext.getDbSqlSession().selectOne("selectAttachmentCountByQueryCriteria", this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Attachment> executeList(CommandContext commandContext, Page page) {
    return commandContext.getDbSqlSession().selectList("selectAttachmentByQueryCriteria", this);
  }
  
}
