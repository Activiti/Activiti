/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.services;

import org.activiti.conf.SecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SecurityPolicyService {

    @Autowired
    private SecurityProperties securityProperties;


    public void updatePolicy(String userOrGroupType, String userOrGroupId, String processDefs, String policy){
        // need to check what the current identity link status is in engine db

        // then map to new policy

        // currently any link at all gives visibility of a process def
        // so 'NONE' would correspond to no link so in that case would need to remove links
        // and also remove any links on proc inst and task...


        // TODO: the engine does distinguish read and write as IdentityLinkEntityManagerImpl.addIdentityLink differentiates CANDIDATE or PARTICIPANT

        // what are the meanings of activiti roles - assignee,candidate,owner,starter,participant ?
        // see https://community.alfresco.com/thread/222547-actors-owners-assignees-candidates
        // seems the distintinctions are mostly about delegating tasks to other users
        // for a given link could add a custom type but how would you then use it?


        // read would correspond to...
        // for proc def participant
        // for proc inst list it would have to be that you are a participant on the proc def...
        // ... in which case we might as well use the property in preference to the db?... HAVE TWO OPTIONS
        // so 1) in PageableProcessInstanceService createProcessInstanceQuery could set processDefinitionKeys to ones user can see
        // or 2) create a new engine query in which it does join to see if user is participant on proc inst's proc def
        // (could find by props or db query but __USING DB QUERY EXACERBATES SYNC RISKS__ like the deletion problem as if use props at least restart would fix, remember there could be multiple RB aps)
        // likewise for task it would have to be that you are a participant or have read on the proc def


        // TODO: a big question is does 'READ' mean anyone in group can see all instances
        // the default behaviour is that only I can see my proc instances
        // others can see tasks on my proc instances if they are candidates but only I see my proc instances
        // I THINK WE SHOULD KEEP THIS BEHAVIOUR

        // write would correspond to
        // HAVE TO ADD CHECKS ON CLAIM, COMPLETE, START ETC. AT THIS LEVEL NOT JUST RESTRICTING OF LISTS (WHICH IS ALL ENGINE DOES)
        // COULD ADD CHECKS AT ENGINE LEVEL BUT THAT IS NOT VERY BACKWARD COMPATIBLE

    }

    public Set<String> getProcessDefinitionKeys(String userId){

        // first

        // TODO: look up groups


    }

    public boolean canPerformWrite(String userId, String processDef){

        // TODO: may use something more fine-grained but need to have a way to check whether user allowed to perform action
    }

}
