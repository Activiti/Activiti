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

package org.activiti.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

public class PropertiesChangedListener implements ApplicationListener<EnvironmentChangeEvent> {

    private final Environment env;

    @Autowired
    public PropertiesChangedListener(Environment env) {
        this.env = env;

    }

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {

        if(event!=null && event.getKeys()!=null){
            for(String key: event.getKeys()){
                if(key.startsWith("activiti.cloud.group") || key.startsWith("activiti.cloud.user")){

                    String keyWithoutPrefix = key.replaceFirst("activti.cloud.","");

                    String userOrGroupType = keyWithoutPrefix.substring(0,keyWithoutPrefix.indexOf('.'));

                    keyWithoutPrefix = keyWithoutPrefix.replaceFirst(userOrGroupType+".","");

                    String userOrGroupId = keyWithoutPrefix.substring(0,keyWithoutPrefix.indexOf('.'));

                    //we now know that something has changed for a given user/group
                    //could be a new policy created, a permission level increased or reduced

                    String policy = env.getProperty("activiti.cloud."+userOrGroupType+"."+userOrGroupId+".policy");
                    String procDefs = env.getProperty("activiti.cloud."+userOrGroupType+"."+userOrGroupId+".processdef");



                    // TODO: call SecurityPolicyService

                    //TODO: what if a property or property file is removed ? that actually seems to be an open issue - https://github.com/spring-cloud/spring-cloud-commons/pull/234/files

                }
            }
        }

    }

}
