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

package org.activiti.rest.service.api.legacy.identity;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineInfo;
import org.activiti.engine.ProcessEngines;
import org.activiti.rest.common.api.ActivitiUtil;
import org.restlet.data.Status;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

/**
 * @author Tijs Rademakers
 */
public class LegacyLoginResource extends ServerResource {
  
  @Post
  public LegacyLoginResponse login(LegacyLoginInfo loginInfo) {
    if(loginInfo == null) {
      throw new ActivitiIllegalArgumentException("No login info supplied");
    }
    
    if(loginInfo.getUserId() == null) {
      throw new ActivitiIllegalArgumentException("No user id supplied");
    }
    
    if(loginInfo.getPassword() == null) {
      throw new ActivitiIllegalArgumentException("No password supplied");
    }
    
    ProcessEngine pe = ActivitiUtil.getProcessEngine();
    if (pe != null) {
      if (pe.getIdentityService().checkPassword(loginInfo.getUserId(), loginInfo.getPassword()) == false) {
        throw new ActivitiException("Username and password does not match.");
      }
      return new LegacyLoginResponse().setSuccess(true);
    
    } else {
      String message;
      ProcessEngineInfo pei = ActivitiUtil.getProcessEngineInfo();
      if (pei != null) {
        message = pei.getException();
      }
      else {
        message = "Can't find process engine which is needed to authenticate username and password.";
        List<ProcessEngineInfo> processEngineInfos = ProcessEngines.getProcessEngineInfos();
        if (processEngineInfos.size() > 0) {
          message += "\nHowever " + processEngineInfos.size() + " other process engine(s) were found: ";
        }
        for (ProcessEngineInfo processEngineInfo : processEngineInfos)
        {
          message += "Process engine '" + processEngineInfo.getName() + "' (" + processEngineInfo.getResourceUrl() + "):";
          if (processEngineInfo.getException() != null) {
            message += processEngineInfo.getException();
          }
          else {
            message += "OK";
          }
        }
      }
      throw new ActivitiException(message);
    }
  }
  
  protected Status getAuthenticationFailureStatus() {
    return Status.CLIENT_ERROR_FORBIDDEN;
  }

}
