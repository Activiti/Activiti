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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("activiti.cloud")
@RefreshScope
public class SecurityProperties implements InitializingBean {

    private Map<String, String> group = new HashMap<String, String>();
    private Map<String, String> user = new HashMap<String,String>();

    public Map<String, String> getGroup() {
        return this.group;
    }

    public Map<String, String> getUser() {
        return this.user;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // TODO: call service to apply any properties to db
    }

}
