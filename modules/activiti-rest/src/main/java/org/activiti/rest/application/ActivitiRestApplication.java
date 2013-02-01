/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.rest.application;

import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DefaultResource;
import org.activiti.rest.api.engine.ProcessEngineResource;
import org.activiti.rest.api.history.HistoricFormPropertiesResource;
import org.activiti.rest.api.identity.GroupCreateResource;
import org.activiti.rest.api.identity.GroupResource;
import org.activiti.rest.api.identity.GroupSearchResource;
import org.activiti.rest.api.identity.GroupUsersResource;
import org.activiti.rest.api.identity.LoginResource;
import org.activiti.rest.api.identity.UserCreateResource;
import org.activiti.rest.api.identity.UserGroupsDeleteResource;
import org.activiti.rest.api.identity.UserGroupsResource;
import org.activiti.rest.api.identity.UserPictureResource;
import org.activiti.rest.api.identity.UserResource;
import org.activiti.rest.api.identity.UserSearchResource;
import org.activiti.rest.api.management.JobExecuteResource;
import org.activiti.rest.api.management.JobResource;
import org.activiti.rest.api.management.JobsExecuteResource;
import org.activiti.rest.api.management.JobsResource;
import org.activiti.rest.api.management.TableDataResource;
import org.activiti.rest.api.management.TableResource;
import org.activiti.rest.api.management.TablesResource;
import org.activiti.rest.api.process.ProcessDefinitionDiagramResource;
import org.activiti.rest.api.process.ProcessDefinitionFormResource;
import org.activiti.rest.api.process.ProcessDefinitionPropertiesResource;
import org.activiti.rest.api.process.ProcessDefinitionsResource;
import org.activiti.rest.api.process.ProcessInstanceDiagramResource;
import org.activiti.rest.api.process.ProcessInstanceResource;
import org.activiti.rest.api.process.ProcessInstanceSignalExecutionResource;
import org.activiti.rest.api.process.ProcessInstanceTaskResource;
import org.activiti.rest.api.process.ProcessInstancesResource;
import org.activiti.rest.api.process.SignalEventSubscriptionResource;
import org.activiti.rest.api.process.StartProcessInstanceResource;
import org.activiti.rest.api.repository.DeploymentArtifactResource;
import org.activiti.rest.api.repository.DeploymentArtifactsResource;
import org.activiti.rest.api.repository.DeploymentDeleteResource;
import org.activiti.rest.api.repository.DeploymentUploadResource;
import org.activiti.rest.api.repository.DeploymentsDeleteResource;
import org.activiti.rest.api.repository.DeploymentsResource;
import org.activiti.rest.api.task.TaskAddResource;
import org.activiti.rest.api.task.TaskAttachmentAddResource;
import org.activiti.rest.api.task.TaskAttachmentResource;
import org.activiti.rest.api.task.TaskFormResource;
import org.activiti.rest.api.task.TaskOperationResource;
import org.activiti.rest.api.task.TaskPropertiesResource;
import org.activiti.rest.api.task.TaskResource;
import org.activiti.rest.api.task.TaskUrlAddResource;
import org.activiti.rest.api.task.TasksResource;
import org.activiti.rest.api.task.TasksSummaryResource;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.SecretVerifier;
import org.restlet.security.Verifier;
import org.restlet.service.StatusService;
/**
 * @author Tijs Rademakers
 */
public class ActivitiRestApplication extends Application {
  
  private ChallengeAuthenticator authenticator;
  private ActivitiStatusService activitiStatusService;

  
  public ActivitiRestApplication() {
    activitiStatusService = new ActivitiStatusService();
  }
  /**
   * Creates a root Restlet that will receive all incoming calls.
   */
  @Override
  public synchronized Restlet createInboundRoot() {
    Verifier verifier = new SecretVerifier() {

      @Override
      public boolean verify(String username, char[] password) throws IllegalArgumentException {
        boolean verified = ActivitiUtil.getIdentityService().checkPassword(username, new String(password));
        return verified;
      }
    };
    authenticator = new ChallengeAuthenticator(null, true, ChallengeScheme.HTTP_BASIC,
          "Activiti Realm") {
      
      @Override
      protected boolean authenticate(Request request, Response response) {
        if (request.getChallengeResponse() == null) {
          return false;
        } else {
          return super.authenticate(request, response);
        }
      }
    };
    authenticator.setVerifier(verifier);
    
    Router router = new Router(getContext());

    router.attachDefault(DefaultResource.class);
    
    router.attach("/process-engine", ProcessEngineResource.class);
    
    router.attach("/login", LoginResource.class);
    
    router.attach("/user", UserCreateResource.class);
    router.attach("/user/{userId}", UserResource.class);
    router.attach("/user/{userId}/groups", UserGroupsResource.class);
    router.attach("/user/{userId}/groups/{groupId}", UserGroupsDeleteResource.class);
    router.attach("/user/{userId}/picture", UserPictureResource.class);
    router.attach("/users", UserSearchResource.class);

    router.attach("/group", GroupCreateResource.class);
    router.attach("/group/{groupId}", GroupResource.class);
    router.attach("/group/{groupId}/users/{userId}", UserGroupsDeleteResource.class);
    router.attach("/group/{groupId}/users", GroupUsersResource.class);
    router.attach("/groups", GroupSearchResource.class);
    
    router.attach("/process-definitions", ProcessDefinitionsResource.class);
    router.attach("/process-instances", ProcessInstancesResource.class);
    router.attach("/process-instance", StartProcessInstanceResource.class);
    router.attach("/process-instance/{processInstanceId}", ProcessInstanceResource.class);
    router.attach("/process-instance/{processInstanceId}/diagram", ProcessInstanceDiagramResource.class);
    router.attach("/process-instance/{processInstanceId}/tasks", ProcessInstanceTaskResource.class);
    router.attach("/process-instance/{processInstanceId}/signal", ProcessInstanceSignalExecutionResource.class);
    router.attach("/process-instance/{processInstanceId}/event/{signalName}", SignalEventSubscriptionResource.class);
    router.attach("/process-definition/{processDefinitionId}/form", ProcessDefinitionFormResource.class);
    router.attach("/process-definition/{processDefinitionId}/diagram", ProcessDefinitionDiagramResource.class);
    router.attach("/process-definition/{processDefinitionId}/properties", ProcessDefinitionPropertiesResource.class);
    
    /*router.attach("/process-instance/{processInstanceId}/highlights", ProcessInstanceHighlightsResource.class);
    router.attach("/process-instance/{processInstanceId}/diagram-layout", ProcessDefinitionDiagramLayoutResource.class);
    router.attach("/process-definition/{processDefinitionId}/diagram-layout", ProcessDefinitionDiagramLayoutResource.class);*/
    
    router.attach("/tasks", TasksResource.class);
    router.attach("/tasks-summary", TasksSummaryResource.class);
    router.attach("/task", TaskAddResource.class);
    router.attach("/task/{taskId}", TaskResource.class);
    router.attach("/task/{taskId}/form", TaskFormResource.class);
    router.attach("/task/{taskId}/attachment", TaskAttachmentAddResource.class);
    router.attach("/task/{taskId}/url", TaskUrlAddResource.class);
    router.attach("/task/{taskId}/{operation}", TaskOperationResource.class);
    
    router.attach("/history/{taskId}/form-properties", HistoricFormPropertiesResource.class);
    
    router.attach("/attachment/{attachmentId}", TaskAttachmentResource.class);
    
    router.attach("/form/{taskId}/properties", TaskPropertiesResource.class);
    
    router.attach("/deployments", DeploymentsResource.class);
    router.attach("/deployment", DeploymentUploadResource.class);
    router.attach("/deployments/delete", DeploymentsDeleteResource.class);
    router.attach("/deployment/{deploymentId}", DeploymentDeleteResource.class);
    router.attach("/deployment/{deploymentId}/resources", DeploymentArtifactsResource.class);
    router.attach("/deployment/{deploymentId}/resource/{resourceName}", DeploymentArtifactResource.class);
    
    router.attach("/management/jobs", JobsResource.class);
    router.attach("/management/job/{jobId}", JobResource.class);
    router.attach("/management/job/{jobId}/execute", JobExecuteResource.class);
    router.attach("/management/jobs/execute", JobsExecuteResource.class);
    
    router.attach("/management/tables", TablesResource.class);
    router.attach("/management/table/{tableName}", TableResource.class);
    router.attach("/management/table/{tableName}/data", TableDataResource.class);
    
    authenticator.setNext(router);
    
    return authenticator;
  }
  
  public String authenticate(Request request, Response response) {
    if (!request.getClientInfo().isAuthenticated()) {
      authenticator.challenge(response, false);
      return null;
    }
    return request.getClientInfo().getUser().getIdentifier();
  }
  
  @Override
  public StatusService getStatusService() {
    return activitiStatusService;
  }
}
